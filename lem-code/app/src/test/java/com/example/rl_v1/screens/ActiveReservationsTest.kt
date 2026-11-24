package com.example.rl_v1.screens

import com.example.rl_v1.model.Reservation
import com.example.rl_v1.ui.theme.screens.owner.activeReservations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ActiveReservationsTest {

    // Every test pretends today is 2027-05-08.
    private val today = "2027-05-08"

    private val past = Reservation(id = "past", checkInDate = "2027-05-01", checkOutDate = "2027-05-03")
    private val endsToday = Reservation(id = "endsToday", checkInDate = "2027-05-06", checkOutDate = "2027-05-08")
    private val ongoing = Reservation(id = "ongoing", checkInDate = "2027-05-07", checkOutDate = "2027-05-10")
    private val future = Reservation(id = "future", checkInDate = "2027-06-01", checkOutDate = "2027-06-03")

    @Test fun noReservationsGivesEmptyList() {
        assertTrue(activeReservations(emptyList(), today).isEmpty())
    }

    @Test fun futureStayIsActive() {
        assertEquals(listOf(future), activeReservations(listOf(future), today))
    }

    @Test fun ongoingStayIsActive() {
        assertEquals(listOf(ongoing), activeReservations(listOf(ongoing), today))
    }

    @Test fun pastStayIsNotActive() {
        assertTrue(activeReservations(listOf(past), today).isEmpty())
    }

    @Test fun stayEndingTodayIsNotActive() {
        assertTrue(activeReservations(listOf(endsToday), today).isEmpty())
    }

    @Test fun rejectedStayIsNotActive() {
        val rejected = future.copy(status = "rejected")
        assertTrue(activeReservations(listOf(rejected), today).isEmpty())
    }

    @Test fun earliestCheckInComesFirst() {
        assertEquals(listOf(ongoing, future), activeReservations(listOf(future, ongoing), today))
    }

    @Test fun mixedListKeepsOnlyActive() {
        val rejected = future.copy(id = "rejected", status = "rejected")
        val all = listOf(past, future, rejected, endsToday, ongoing)
        assertEquals(listOf(ongoing, future), activeReservations(all, today))
    }
}
