package com.example.rl_v1.repository

import com.example.rl_v1.model.DateAvailability
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReservationDatesTest {

    @Test fun twoNightStay() {
        val nights = stayNights("2027-05-07", "2027-05-09").getOrThrow()
        assertEquals(listOf(LocalDate.of(2027, 5, 7), LocalDate.of(2027, 5, 8)), nights)
    }

    @Test fun sameDayIsRejected() {
        assertTrue(stayNights("2027-05-07", "2027-05-07").isFailure)
    }

    @Test fun checkOutBeforeCheckInIsRejected() {
        assertTrue(stayNights("2027-05-09", "2027-05-07").isFailure)
    }

    @Test fun badFormatIsRejected() {
        assertTrue(stayNights("05/07/2027", "2027-05-09").isFailure)
    }

    @Test fun thirtyNightsAllowedThirtyOneRejected() {
        assertTrue(stayNights("2027-05-01", "2027-05-31").isSuccess)
        assertTrue(stayNights("2027-05-01", "2027-06-01").isFailure)
    }

    @Test fun blankAvailabilityMeansNoLimit() {
        val nights = stayNights("2027-05-07", "2027-05-09").getOrThrow()
        assertTrue(withinAvailability(nights, DateAvailability("", "")))
    }

    @Test fun stayInsideWindowIsAvailable() {
        val nights = stayNights("2027-05-07", "2027-05-09").getOrThrow()
        assertTrue(withinAvailability(nights, DateAvailability("2027-05-01", "2027-05-08")))
    }

    @Test fun stayOutsideWindowIsUnavailable() {
        val nights = stayNights("2027-05-07", "2027-05-09").getOrThrow()
        assertFalse(withinAvailability(nights, DateAvailability("2027-05-08", "")))
        assertFalse(withinAvailability(nights, DateAvailability("", "2027-05-07")))
    }
}
