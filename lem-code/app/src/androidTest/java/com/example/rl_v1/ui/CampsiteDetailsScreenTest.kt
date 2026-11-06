package com.example.rl_v1.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.rl_v1.ui.theme.screens.explore.CampsiteDetailsScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CampsiteDetailsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test fun showsNameAndLocation() {
        composeRule.setContent { CampsiteDetailsScreen(hintonPines, onBack = {}) }

        composeRule.onNodeWithText("Hinton Pines").assertIsDisplayed()
        composeRule.onNodeWithText("Hinton, AB").assertIsDisplayed()
    }

    @Test fun chooseDatesButtonWorks() {
        var booked = false
        composeRule.setContent {
            CampsiteDetailsScreen(hintonPines, onBack = {}, onBook = { booked = true })
        }

        composeRule.onNodeWithText("Choose dates").performScrollTo().performClick()

        assertTrue(booked)
    }
}
