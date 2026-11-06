package com.example.rl_v1.repository

import com.example.rl_v1.model.Reservation
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.math.abs

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
            val availableFrom = campsite.getString("availableFrom").orEmpty()
            val availableTo = campsite.getString("availableTo").orEmpty()

            if (numberOfGuests < 1 || numberOfGuests > maxOccupancy) {
                throw IllegalStateException("This campsite allows 1 to $maxOccupancy guests")
            }
            if (!withinAvailability(nights, availableFrom, availableTo)) {
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
