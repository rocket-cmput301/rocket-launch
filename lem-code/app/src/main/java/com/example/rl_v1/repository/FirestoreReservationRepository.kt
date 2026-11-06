package com.example.rl_v1.repository

import com.example.rl_v1.model.DateAvailability
import com.example.rl_v1.model.Reservation
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.math.abs

/**
 * Checks whether [ownerId] is allowed to reject [reservation].
 * Returns the error message to show, or null if the reservation can be rejected.
 */
fun rejectError(reservation: Reservation, ownerId: String): String? {
    if (reservation.ownerId != ownerId) {
        return "You can only reject reservations on your campsites"
    }
    if (reservation.status == "rejected") {
        return "This reservation was already rejected"
    }
    return null
}

/**
 * Saves and observes reservations in Cloud Firestore.
 *
 * Each booked night is also written to `bookedNights/{campsiteId}_{date}`.
 * Booking runs in a transaction that fails if any of those night documents
 * already exists, so two entrants can never book the same night.
 * Cancelling a reservation is not implemented yet.
 */
class FirestoreReservationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ReservationRepository {

    private val campsites = firestore.collection("campsites")
    private val reservations = firestore.collection("reservations")
    private val bookedNights = firestore.collection("bookedNights")

    override suspend fun book(
        campsiteId: String,
        entrantId: String,
        checkInDate: String,
        checkOutDate: String,
        numberOfGuests: Int,
        totalPrice: Double
    ): Result<Reservation> = try {
        val nights = stayNights(checkInDate, checkOutDate).getOrThrow()
        require(entrantId.isNotBlank()) { "Sign in before booking" }

        val reservation = firestore.runTransaction { transaction ->
            // All reads must happen before any writes in a transaction.
            val campsite = transaction.get(campsites.document(campsiteId))
            if (!campsite.exists()) throw IllegalStateException("This campsite no longer exists")

            val ownerId = campsite.getString("ownerId").orEmpty()
            val maxOccupancy = campsite.getLong("maxOccupancy") ?: 0L
            val pricePerNight = campsite.getDouble("pricePerNight") ?: 0.0
            val availabilityData = campsite.get("availability") as? Map<*, *>

            val availability = availabilityData?.let {
                DateAvailability(
                    startDate = it["startDate"] as? String ?: "",
                    endDate = it["endDate"] as? String ?: ""
                )
            }

            if (numberOfGuests < 1 || numberOfGuests > maxOccupancy) {
                throw IllegalStateException("This campsite allows 1 to $maxOccupancy guests")
            }
            if (availability == null || !withinAvailability(nights, availability)) {
                throw IllegalStateException("The campsite isn't available on those dates")
            }
            val expectedTotal = nights.size * pricePerNight
            if (abs(expectedTotal - totalPrice) > 0.01) {
                throw IllegalStateException("The price changed, please review your booking again")
            }

            val nightRefs = nights.map { bookedNights.document("${campsiteId}_$it") }
            for (ref in nightRefs) {
                if (transaction.get(ref).exists()) {
                    throw IllegalStateException("Some of those nights are already booked")
                }
            }

            val reservationRef = reservations.document()
            val saved = Reservation(
                id = reservationRef.id,
                campsiteId = campsiteId,
                ownerId = ownerId,
                entrantId = entrantId,
                checkInDate = checkInDate,
                checkOutDate = checkOutDate,
                numberOfGuests = numberOfGuests,
                totalPrice = expectedTotal
            )

            transaction.set(reservationRef, saved)
            nights.forEachIndexed { index, night ->
                transaction.set(
                    nightRefs[index],
                    mapOf(
                        "campsiteId" to campsiteId,
                        "reservationId" to reservationRef.id,
                        "entrantId" to entrantId,
                        "date" to night.toString()
                    )
                )
            }
            saved
        }.await()

        Result.success(reservation)
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }

    override suspend fun reject(reservationId: String, ownerId: String): Result<Unit> = try {
        firestore.runTransaction { transaction ->
            val reservationRef = reservations.document(reservationId)
            val reservation = transaction.get(reservationRef).toObject(Reservation::class.java)

            if (reservation == null) {
                throw IllegalStateException("This reservation no longer exists")
            }
            val error = rejectError(reservation, ownerId)
            if (error != null) {
                throw IllegalStateException(error)
            }

            val nights = stayNights(reservation.checkInDate, reservation.checkOutDate).getOrThrow()

            // Find the booked nights that still belong to this reservation.
            // All reads must happen before any writes in a transaction.
            val nightsToFree = mutableListOf<DocumentReference>()
            for (night in nights) {
                val nightRef = bookedNights.document("${reservation.campsiteId}_$night")
                val nightDoc = transaction.get(nightRef)
                if (nightDoc.getString("reservationId") == reservationId) {
                    nightsToFree.add(nightRef)
                }
            }

            // Mark the reservation rejected and free its nights so they can be booked again.
            transaction.update(reservationRef, "status", "rejected")
            for (nightRef in nightsToFree) {
                transaction.delete(nightRef)
            }
        }.await()
        Result.success(Unit)
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }

    override fun viewEntrantReservations(entrantId: String): Flow<List<Reservation>> =
        observeQuery(reservations.whereEqualTo("entrantId", entrantId))

    override fun viewOwnerReservations(ownerId: String): Flow<List<Reservation>> =
        observeQuery(reservations.whereEqualTo("ownerId", ownerId))

    private fun observeQuery(query: Query): Flow<List<Reservation>> =
        callbackFlow {
            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                } else if (snapshot != null) {
                    try {
                        val list = snapshot.documents
                            .mapNotNull { document ->
                                document.toObject(Reservation::class.java)
                                    ?.copy(id = document.id)
                            }
                            .sortedBy { it.checkInDate }
                        trySend(list)
                    } catch (error: Exception) {
                        close(error)
                    }
                }
            }
            awaitClose { listener.remove() }
        }
}
