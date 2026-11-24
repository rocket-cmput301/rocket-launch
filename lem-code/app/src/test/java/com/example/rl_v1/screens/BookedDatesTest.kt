package com.example.rl_v1.screens

import com.example.rl_v1.model.Reservation
import com.example.rl_v1.ui.theme.screens.owner.bookedDatesFor
import org.junit.Assert.assertEquals
import org.junit.Test

class BookedDatesTest {

    private val may = Reservation(id = "1", campsiteId = "hinton", checkInDate = "2027-05-07", checkOutDate = "2027-05-09")
    private val june = Reservation(id = "2", campsiteId = "hinton", checkInDate = "2027-06-01", checkOutDate = "2027-06-03")
    private val otherCampsite = Reservation(id = "3", campsiteId = "lake", checkInDate = "2027-05-20", checkOutDate = "2027-05-21")

    @Test fun noReservationsShowsANote() {
        assertEquals("No booked dates yet.", bookedDatesFor("hinton", emptyList()))
    }

    @Test fun oneReservation() {
        assertEquals("2027-05-07 to 2027-05-09", bookedDatesFor("hinton", listOf(may)))
    }

    @Test fun onlyShowsThisCampsite() {
        assertEquals("2027-05-07 to 2027-05-09", bookedDatesFor("hinton", listOf(may, otherCampsite)))
    }

    @Test fun onlyOtherCampsitesShowsANote() {
        assertEquals("No booked dates yet.", bookedDatesFor("hinton", listOf(otherCampsite)))
    }

    @Test fun eachStayOnItsOwnLineEarliestFirst() {
        assertEquals(
            "2027-05-07 to 2027-05-09\n2027-06-01 to 2027-06-03",
            bookedDatesFor("hinton", listOf(june, may))
        )
    }

    @Test fun rejectedReservationsAreSkipped() {
        val rejected = june.copy(status = "rejected")
        assertEquals("2027-05-07 to 2027-05-09", bookedDatesFor("hinton", listOf(may, rejected)))
    }

    @Test fun cancelledReservationsAreSkipped() {
        val cancelled = june.copy(status = "cancelled")
        assertEquals("2027-05-07 to 2027-05-09", bookedDatesFor("hinton", listOf(may, cancelled)))
    }

    @Test fun onlyRejectedShowsANote() {
        val rejected = may.copy(status = "rejected")
        assertEquals("No booked dates yet.", bookedDatesFor("hinton", listOf(rejected)))
    }
}
