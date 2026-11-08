package com.example.rl_v1.repository

import com.example.rl_v1.model.Reservation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RejectReservationTest {

    private val reservation = Reservation(
        id = "r1", campsiteId = "c1", ownerId = "owner", entrantId = "camper",
        checkInDate = "2027-05-07", checkOutDate = "2027-05-09",
        numberOfGuests = 2, totalPrice = 100.0
    )

    @Test fun newReservationIsConfirmed() {
        assertEquals("confirmed", reservation.status)
    }

    @Test fun ownerCanRejectConfirmedReservation() {
        assertNull(rejectError(reservation, "owner"))
    }

    @Test fun someoneElseCannotReject() {
        assertNotNull(rejectError(reservation, "camper"))
        assertNotNull(rejectError(reservation, "stranger"))
    }

    @Test fun alreadyRejectedCannotBeRejectedAgain() {
        val rejected = reservation.copy(status = "rejected")
        assertNotNull(rejectError(rejected, "owner"))
    }
}
