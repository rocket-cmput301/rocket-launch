package com.example.rl_v1.screens

import com.example.rl_v1.model.Reservation
import com.example.rl_v1.ui.theme.screens.owner.cancelledReservations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CancelledReservationsTest {

    private val confirmed = Reservation(id = "confirmed", checkInDate = "2027-05-01", checkOutDate = "2027-05-03")
    private val rejected = Reservation(id = "rejected", checkInDate = "2027-05-07", checkOutDate = "2027-05-09", status = "rejected")
    private val cancelled = Reservation(id = "cancelled", checkInDate = "2027-06-01", checkOutDate = "2027-06-03", status = "cancelled")

    @Test fun noReservationsGivesEmptyList() {
        assertTrue(cancelledReservations(emptyList()).isEmpty())
    }

    @Test fun rejectedIsIncluded() {
        assertEquals(listOf(rejected), cancelledReservations(listOf(rejected)))
    }

    @Test fun cancelledIsIncluded() {
        assertEquals(listOf(cancelled), cancelledReservations(listOf(cancelled)))
    }

    @Test fun confirmedIsNotIncluded() {
        assertTrue(cancelledReservations(listOf(confirmed)).isEmpty())
    }

    @Test fun mostRecentCheckInComesFirst() {
        assertEquals(listOf(cancelled, rejected), cancelledReservations(listOf(rejected, cancelled)))
    }

    @Test fun mixedListKeepsOnlyCancelled() {
        assertEquals(listOf(cancelled, rejected), cancelledReservations(listOf(confirmed, rejected, cancelled)))
    }
}
