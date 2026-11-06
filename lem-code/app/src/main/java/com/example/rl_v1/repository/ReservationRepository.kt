/*
This is the interface for the reservation repository.

 */

package com.example.rl_v1.repository
import com.example.rl_v1.model.Reservation
import kotlinx.coroutines.flow.Flow

/** Reads and writes campsite reservations. */
interface ReservationRepository {

    /** Books a campsite for the given dates. Fails if the campsite doesn't exist or a night is already taken. */
    suspend fun book(
        campsiteId: String,
        entrantId: String,
        checkInDate: String,
        checkOutDate: String,
        numberOfGuests: Int,
        totalPrice: Double
    ): Result<Reservation>

    /** Reservations made by [entrantId], updated live. */
    fun viewEntrantReservations(entrantId: String): Flow<List<Reservation>>

    /** Reservations made on [ownerId]'s campsites, updated live. */
    fun viewOwnerReservations(ownerId: String): Flow<List<Reservation>>

    // suspend = function that has to wait on something slow
    /** Rejects a reservation on one of [ownerId]'s campsites and frees its nights. */
    suspend fun reject(reservationId: String, ownerId: String): Result<Unit>
}
