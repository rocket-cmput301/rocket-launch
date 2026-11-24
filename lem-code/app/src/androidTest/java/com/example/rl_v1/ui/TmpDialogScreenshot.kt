package com.example.rl_v1.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.rl_v1.ui.theme.theme.Rlv1Theme
import com.example.rl_v1.ui.theme.screens.reservation.ReservationConfirmedScreen
import org.junit.Rule
import org.junit.Test

// Temporary: holds the cancel dialog on screen for a screenshot. Delete after use.
class TmpDialogScreenshot {
    @get:Rule val composeRule = createComposeRule()

    @Test fun hold() {
        composeRule.setContent {
            Rlv1Theme {
                ReservationConfirmedScreen(testReservation, "Hinton Pines", onDone = {}, canCancel = true)
            }
        }
        composeRule.onNodeWithText("Cancel reservation").performClick()
        composeRule.waitForIdle()
        Thread.sleep(25000)
    }
}
