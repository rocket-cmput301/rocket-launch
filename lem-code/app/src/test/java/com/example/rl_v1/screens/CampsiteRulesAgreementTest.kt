package com.example.rl_v1.screens

import com.example.rl_v1.model.Campsite
import com.example.rl_v1.ui.theme.screens.reservation.canConfirmReservation
import com.example.rl_v1.ui.theme.screens.reservation.policyToShow
import com.example.rl_v1.ui.theme.screens.reservation.rulesToShow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CampsiteRulesAgreementTest {

    private val campsite = Campsite(
        id = "1", ownerId = "owner", name = "Hinton Pines", location = "Hinton, AB",
        maxOccupancy = 4, pricePerNight = 50.0,
        rules = "No fires after 10pm", cancellationPolicy = "Full refund up to 7 days before"
    )

    @Test fun rulesAndPolicyAreShown() {
        assertEquals("No fires after 10pm", rulesToShow(campsite))
        assertEquals("Full refund up to 7 days before", policyToShow(campsite))
    }

    @Test fun blankRulesAndPolicyShowANote() {
        val noRules = campsite.copy(rules = "  ", cancellationPolicy = "")
        assertEquals("This campsite has no extra rules.", rulesToShow(noRules))
        assertEquals("This campsite has no cancellation policy.", policyToShow(noRules))
    }

    @Test fun canConfirmOnceRulesAreAgreed() {
        assertTrue(canConfirmReservation(datesValid = true, guestsValid = true, agreedToRules = true))
    }

    @Test fun cannotConfirmWithoutAgreeingToRules() {
        assertFalse(canConfirmReservation(datesValid = true, guestsValid = true, agreedToRules = false))
    }

    @Test fun agreeingIsNotEnoughWithBadDatesOrGuests() {
        assertFalse(canConfirmReservation(datesValid = false, guestsValid = true, agreedToRules = true))
        assertFalse(canConfirmReservation(datesValid = true, guestsValid = false, agreedToRules = true))
    }
}
