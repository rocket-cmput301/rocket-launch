package com.example.rl_v1.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

// tests reservation history filtering checkout boundaries and ordering
class ReservationHistoryTest {

    private val today = LocalDate.of(2026, 11, 16)

    @Test
    fun activeIncludesOngoingAndUpcomingStaysInCheckInOrder() {
        val upcoming = Reservation(
            id = "upcoming",
            checkInDate = "2026-11-20",
            checkOutDate = "2026-11-22"
        )
        val ongoing = Reservation(
            id = "ongoing",
            checkInDate = "2026-11-15",
            checkOutDate = "2026-11-17"
        )
        val past = Reservation(
            id = "past",
            checkInDate = "2026-11-10",
            checkOutDate = "2026-11-12"
        )

        val result = ReservationHistory.active(
            listOf(upcoming, past, ongoing),
            today
        )

        assertEquals(listOf("ongoing", "upcoming"), result.map { it.id })
    }

    @Test
    fun checkoutTodayCountsAsPast() {
        val reservation = Reservation(
            id = "checkout-today",
            checkInDate = "2026-11-14",
            checkOutDate = "2026-11-16"
        )

        assertTrue(
            ReservationHistory.active(listOf(reservation), today).isEmpty()
        )
        assertEquals(
            listOf(reservation),
            ReservationHistory.past(listOf(reservation), today)
        )
    }

    @Test
    fun pastShowsMostRecentCheckoutFirst() {
        val older = Reservation(
            id = "older",
            checkInDate = "2026-10-01",
            checkOutDate = "2026-10-03"
        )
        val recent = Reservation(
            id = "recent",
            checkInDate = "2026-11-12",
            checkOutDate = "2026-11-14"
        )

        val result = ReservationHistory.past(
            listOf(older, recent),
            today
        )

        assertEquals(listOf("recent", "older"), result.map { it.id })
    }

    @Test
    fun rejectedReservationsAreExcludedFromBothLists() {
        val reservations = listOf(
            Reservation(
                id = "rejected-future",
                checkInDate = "2026-11-20",
                checkOutDate = "2026-11-22",
                status = "rejected"
            ),
            Reservation(
                id = "rejected-past",
                checkInDate = "2026-11-10",
                checkOutDate = "2026-11-12",
                status = "rejected"
            )
        )

        assertTrue(ReservationHistory.active(reservations, today).isEmpty())
        assertTrue(ReservationHistory.past(reservations, today).isEmpty())
    }

    @Test
    fun invalidCheckoutDatesAreExcludedWithoutCrashing() {
        val reservations = listOf(
            Reservation(id = "missing", checkOutDate = ""),
            Reservation(id = "invalid", checkOutDate = "invalid-date")
        )

        assertTrue(ReservationHistory.active(reservations, today).isEmpty())
        assertTrue(ReservationHistory.past(reservations, today).isEmpty())
    }

    @Test
    fun emptyInputProducesEmptyLists() {
        assertTrue(ReservationHistory.active(emptyList(), today).isEmpty())
        assertTrue(ReservationHistory.past(emptyList(), today).isEmpty())
    }
}