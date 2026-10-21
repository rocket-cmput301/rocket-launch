/*
This is the interface for the reservation repository.

 */

package com.example.rl_v1.repository
import com.example.rl_v1.model.Reservation
import kotlinx.coroutines.flow.Flow

/** Reads and writes campsite reservations. */
interface ReservationRepository {

    //fails if the campsite is already booked or if it doesn't exist
    suspend fun book(
        campsiteId: String,
        entrantId: String,
        checkInDate: String,
        checkOutDate: String,
        numberOfGuests: Int,
        totalPrice: Double
    ): Result<Reservation>

    // what the entrant will see
    fun viewEntrantReservations(entrantId: String): Flow<List<Reservation>>

    //what the owner sees
    fun viewOwnerReservations(ownerId: String): Flow<List<Reservation>>
}
