package com.example.rl_v1.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.rl_v1.ui.theme.screens.activity.ActivityScreen
import org.junit.Rule
import org.junit.Test

class ActivityScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test fun showsAReservation() {
        val vm = fakeViewModel(reservations = FakeReservationRepository(listOf(testReservation)))
        composeRule.setContent { ActivityScreen(vm) }

        composeRule.onNodeWithText("Reservations").performClick()

        composeRule.waitUntil(5000) {
            composeRule.onAllNodesWithText("CONFIRMED • HINTON PINES").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("2027-05-07 to 2027-05-09 • 2 guests • $130.00").assertIsDisplayed()
    }

    @Test fun pastFilterShowsCancelledButNotActive() {
        val cancelled = testReservation.copy(id = "res2", status = "cancelled")
        val vm = fakeViewModel(reservations = FakeReservationRepository(listOf(testReservation, cancelled)))
        composeRule.setContent { ActivityScreen(vm) }

        composeRule.onNodeWithText("Reservations").performClick()
        composeRule.onNodeWithText("Past").performClick()

        composeRule.waitUntil(5000) {
            composeRule.onAllNodesWithText("CANCELLED • HINTON PINES").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("CONFIRMED • HINTON PINES").assertDoesNotExist()
    }

    @Test fun addedLottery(){
        //will do later
    }

    @Test fun removedLottery(){
        //will do later
    }
}
