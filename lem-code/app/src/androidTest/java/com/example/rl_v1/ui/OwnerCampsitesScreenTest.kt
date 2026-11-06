package com.example.rl_v1.ui

import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.example.rl_v1.ui.theme.screens.owner.OwnerCampsitesScreen
import org.junit.Rule
import org.junit.Test

class OwnerCampsitesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    /** Waits until some text shows up, since the fake data loads a moment after the screen. */
    private fun waitForText(text: String) {
        composeRule.waitUntil(5000) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test fun rejectingAReservation() {
        val vm = fakeViewModel(reservations = FakeReservationRepository(listOf(testReservation)))
        composeRule.setContent { OwnerCampsitesScreen(vm, onBack = {}, onPublish = {}, onEdit = {}) }
        waitForText("Reject")

        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("Reject"))
        composeRule.onNodeWithText("Reject").performClick()

        // The reservation is marked rejected and its Reject button goes away.
        waitForText("REJECTED • HINTON PINES")
        composeRule.onNodeWithText("Reject").assertDoesNotExist()
    }
}
