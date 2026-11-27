package com.example.rl_v1.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.example.rl_v1.model.DateAvailability
import com.example.rl_v1.model.Lottery
import com.example.rl_v1.ui.theme.screens.inbox.InboxScreen
import org.junit.Rule
import org.junit.Test

class InboxScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val wonLottery = Lottery(
        id = "lot1", campsiteIds = listOf("hinton"), bookingPeriod = DateAvailability("2027-04-01", "2027-05-01")
    )

    @Test fun showsAWinAndAReservation() {
        val vm = fakeViewModel(
            reservations = FakeReservationRepository(listOf(testReservation)),
            lotteries = FakeLotteryRepository(won = listOf(wonLottery))
        )
        composeRule.setContent { InboxScreen(vm) }

        composeRule.waitUntil(5000) {
            composeRule.onAllNodesWithText("SELECTED").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Hinton Pines · Choose your dates before 2027-05-01").assertIsDisplayed()
        composeRule.onNodeWithText("RESERVED").assertIsDisplayed()
        composeRule.onNodeWithText("Hinton Pines · 2027-05-07 to 2027-05-09 · Confirmation res1").assertIsDisplayed()
    }

    @Test fun cancelledReservationShowsCancelled() {
        val vm = fakeViewModel(reservations = FakeReservationRepository(listOf(testReservation.copy(status = "cancelled"))))
        composeRule.setContent { InboxScreen(vm) }

        composeRule.waitUntil(5000) {
            composeRule.onAllNodesWithText("CANCELLED").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("RESERVED").assertDoesNotExist()
    }

    @Test fun emptyInboxSaysSo() {
        composeRule.setContent { InboxScreen(fakeViewModel()) }

        composeRule.onNodeWithText("No notifications yet", substring = true).assertIsDisplayed()
    }
}
