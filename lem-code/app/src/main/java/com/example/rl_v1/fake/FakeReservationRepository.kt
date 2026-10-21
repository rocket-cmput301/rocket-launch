package com.example.rl_v1.fake

import com.example.rl_v1.model.Reservation
import com.example.rl_v1.repository.ReservationRepository
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** [ReservationRepository] backed by [FakeBackend]. */
class FakeReservationRepository : ReservationRepository {

    override suspend fun book(
        campsiteId: String,
        entrantId: String,
        checkInDate: String,
        checkOutDate: String,
        numberOfGuests: Int,
        totalPrice: Double
    ): Result<Reservation> {

        val campsite = FakeBackend.campsites.value.firstOrNull { it.id == campsiteId }
            ?: return Result.failure(IllegalArgumentException("Campsite not found"))

        val checkIn: LocalDate
        val checkOut: LocalDate
        try {
            checkIn = LocalDate.parse(checkInDate)
            checkOut = LocalDate.parse(checkOutDate)
        } catch (e: DateTimeParseException) {
            return Result.failure(IllegalArgumentException("Dates must look like 2027-05-07"))
        }

        if (!checkOut.isAfter(checkIn)) {
            return Result.failure(IllegalArgumentException("Check-out must be after check-in"))
        }

        // The overlap check and the add happen inside one update, so two bookings
        // can't both pass the check and then both be saved.
        var result: Result<Reservation> = Result.failure(IllegalStateException("Not run"))

        FakeBackend.reservations.update { current ->
            // Valid ISO dates sort correctly as plain strings, so < and > work here.
            val clash = current.any {
                it.campsiteId == campsiteId &&
                        checkInDate < it.checkOutDate &&
                        checkOutDate > it.checkInDate
            }

            if (clash) {
                result = Result.failure(IllegalStateException("Those dates are already booked"))
                current
            } else {
                val reservation = Reservation(
                    id = UUID.randomUUID().toString(),
                    campsiteId = campsiteId,
                    ownerId = campsite.ownerId,
                    entrantId = entrantId,
                    checkInDate = checkInDate,
                    checkOutDate = checkOutDate,
                    numberOfGuests = numberOfGuests,
                    totalPrice = totalPrice
                )
                result = Result.success(reservation)
                current + reservation
            }
        }

        return result
    }

    override fun viewEntrantReservations(entrantId: String): Flow<List<Reservation>> =
        FakeBackend.reservations.map { list -> list.filter { it.entrantId == entrantId } }

    override fun viewOwnerReservations(ownerId: String): Flow<List<Reservation>> =
        FakeBackend.reservations.map { list -> list.filter { it.ownerId == ownerId } }
}