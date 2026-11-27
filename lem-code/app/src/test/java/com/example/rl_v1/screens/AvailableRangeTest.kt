package com.example.rl_v1.screens

import com.example.rl_v1.model.Campsite
import com.example.rl_v1.model.DateAvailability
import com.example.rl_v1.ui.theme.screens.reservation.availableRange
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class AvailableRangeTest {

    @Test fun usesTheCampsitesAvailability() {
        val campsite = Campsite(availability = DateAvailability("11/23/2026", "11/29/2026"))

        assertEquals(LocalDate.of(2026, 11, 23) to LocalDate.of(2026, 11, 29), availableRange(campsite))
    }

    @Test fun oldCampsiteWithoutAvailabilityHasNoLimit() {
        assertEquals(LocalDate.MIN to LocalDate.MAX, availableRange(Campsite()))
    }

    @Test fun blankOrBadDatesHaveNoLimit() {
        val campsite = Campsite(availability = DateAvailability("", "2026-11-29"))

        assertEquals(LocalDate.MIN to LocalDate.MAX, availableRange(campsite))
    }
}
