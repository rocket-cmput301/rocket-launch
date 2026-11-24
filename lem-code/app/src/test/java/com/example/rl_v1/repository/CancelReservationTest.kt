package com.example.rl_v1.repository

import com.example.rl_v1.model.Reservation
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CancelReservationTest {

    private val today = "2027-05-01"
    private val reservation = Reservation(
        id = "r1", campsiteId = "c1", ownerId = "owner", entrantId = "camper",
        checkInDate = "2027-05-07", checkOutDate = "2027-05-09",
        numberOfGuests = 2, totalPrice = 100.0
    )

    @Test fun entrantCanCancelBeforeCheckIn() {
        assertNull(cancelError(reservation, "camper", today))
    }

    @Test fun someoneElseCannotCancel() {
        assertNotNull(cancelError(reservation, "owner", today))
        assertNotNull(cancelError(reservation, "stranger", today))
    }

    @Test fun cannotCancelOnOrAfterCheckIn() {
        assertNotNull(cancelError(reservation, "camper", "2027-05-07"))
        assertNotNull(cancelError(reservation, "camper", "2027-05-10"))
    }

    @Test fun cannotCancelTwiceOrAfterRejection() {
        assertNotNull(cancelError(reservation.copy(status = "cancelled"), "camper", today))
        assertNotNull(cancelError(reservation.copy(status = "rejected"), "camper", today))
    }

    @Test fun ownerCannotRejectACancelledReservation() {
        assertNotNull(rejectError(reservation.copy(status = "cancelled"), "owner"))
    }

    @Test fun upcomingStayIsActive() {
        assertFalse(isPast(reservation, today))
    }

    @Test fun finishedCancelledAndRejectedStaysArePast() {
        assertTrue(isPast(reservation, "2027-05-09"))
        assertTrue(isPast(reservation.copy(status = "cancelled"), today))
        assertTrue(isPast(reservation.copy(status = "rejected"), today))
    }
}
