package com.example.rl_v1.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.rl_v1.ui.theme.screens.reservation.SelectDatesScreen
import org.junit.Rule
import org.junit.Test

class SelectDatesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test fun showsTheRulesAndPolicy() {
        composeRule.setContent { SelectDatesScreen(hintonPines, null, onBack = {}, onConfirm = { _, _, _ -> }) }

        composeRule.onNodeWithText("Quiet hours 10 pm to 7 am").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Free cancellation up to 7 days before").performScrollTo().assertIsDisplayed()
    }

    @Test fun checkboxCanBeTicked() {
        composeRule.setContent { SelectDatesScreen(hintonPines, null, onBack = {}, onConfirm = { _, _, _ -> }) }

        val checkbox = composeRule.onNode(isToggleable())
        checkbox.performScrollTo()
        checkbox.assertIsOff()
        checkbox.performClick()
        checkbox.assertIsOn()
    }

    @Test fun confirmIsDisabledWithoutDates() {
        composeRule.setContent { SelectDatesScreen(hintonPines, null, onBack = {}, onConfirm = { _, _, _ -> }) }

        // Even after agreeing to the rules, no dates are picked yet.
        composeRule.onNode(isToggleable()).performScrollTo().performClick()

        composeRule.onNodeWithText("Confirm Reservation").performScrollTo().assertIsNotEnabled()
    }
}
