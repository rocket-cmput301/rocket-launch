package com.example.rl_v1.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.rl_v1.ui.theme.screens.reservation.ReservationConfirmedScreen
import org.junit.Rule
import org.junit.Test

class ReservationConfirmedScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test fun showsTheReservationDetails() {
        composeRule.setContent { ReservationConfirmedScreen(testReservation, "Hinton Pines", onDone = {}) }

        composeRule.onNodeWithText("CONFIRMED • HINTON PINES").assertIsDisplayed()
        composeRule.onNodeWithText("2027-05-07 to 2027-05-09 • 2 guests").assertIsDisplayed()
        composeRule.onNodeWithText("$130.00").assertIsDisplayed()
        composeRule.onNodeWithText("res1").assertIsDisplayed()
    }
}
