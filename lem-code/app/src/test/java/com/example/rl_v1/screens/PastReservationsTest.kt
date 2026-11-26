package com.example.rl_v1.screens

import com.example.rl_v1.model.Reservation
import com.example.rl_v1.ui.theme.screens.owner.pastReservations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PastReservationsTest {

    // Every test pretends today is 2027-05-08.
    private val today = "2027-05-08"

    private val older = Reservation(id = "older", checkInDate = "2027-04-01", checkOutDate = "2027-04-03")
    private val past = Reservation(id = "past", checkInDate = "2027-05-01", checkOutDate = "2027-05-03")
    private val endsToday = Reservation(id = "endsToday", checkInDate = "2027-05-06", checkOutDate = "2027-05-08")
    private val ongoing = Reservation(id = "ongoing", checkInDate = "2027-05-07", checkOutDate = "2027-05-10")
    private val future = Reservation(id = "future", checkInDate = "2027-06-01", checkOutDate = "2027-06-03")

    @Test fun noReservationsGivesEmptyList() {
        assertTrue(pastReservations(emptyList(), today).isEmpty())
    }

    @Test fun pastStayIsPast() {
        assertEquals(listOf(past), pastReservations(listOf(past), today))
    }

    @Test fun stayEndingTodayIsPast() {
        assertEquals(listOf(endsToday), pastReservations(listOf(endsToday), today))
    }

    @Test fun ongoingStayIsNotPast() {
        assertTrue(pastReservations(listOf(ongoing), today).isEmpty())
    }

    @Test fun futureStayIsNotPast() {
        assertTrue(pastReservations(listOf(future), today).isEmpty())
    }

    @Test fun rejectedStayIsNotPast() {
        val rejected = past.copy(status = "rejected")
        assertTrue(pastReservations(listOf(rejected), today).isEmpty())
    }

    @Test fun cancelledStayIsNotPast() {
        val cancelled = past.copy(status = "cancelled")
        assertTrue(pastReservations(listOf(cancelled), today).isEmpty())
    }

    @Test fun mostRecentCheckOutComesFirst() {
        assertEquals(listOf(endsToday, past, older), pastReservations(listOf(older, endsToday, past), today))
    }

    @Test fun mixedListKeepsOnlyPast() {
        val rejected = older.copy(id = "rejected", status = "rejected")
        val all = listOf(future, older, rejected, ongoing, past)
        assertEquals(listOf(past, older), pastReservations(all, today))
    }
}
