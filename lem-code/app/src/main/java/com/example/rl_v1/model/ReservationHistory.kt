package com.example.rl_v1.model

import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * groups confirmed reservations by checkout date for the reservation history
 * checkout times are currently unavailable so stays become past on checkout day
 */
object ReservationHistory {

    // returns upcoming and ongoing stays, ordered by check-in date
    fun active(
        reservations: List<Reservation>,
        today: LocalDate
    ): List<Reservation> =
        reservations.filter { reservation ->
            val checkout = checkoutDate(reservation)
            reservation.status == "confirmed" &&
                    checkout != null &&
                    checkout.isAfter(today)
        }.sortedBy { it.checkInDate }

    // returns completed stays with the most recent checkout first
    fun past(
        reservations: List<Reservation>,
        today: LocalDate
    ): List<Reservation> =
        reservations.filter { reservation ->
            val checkout = checkoutDate(reservation)
            reservation.status == "confirmed" &&
                    checkout != null &&
                    !checkout.isAfter(today)
        }.sortedByDescending { it.checkOutDate }

    private fun checkoutDate(reservation: Reservation): LocalDate? =
        try {
            LocalDate.parse(reservation.checkOutDate)
        } catch (error: DateTimeParseException) {
            null
        }
}