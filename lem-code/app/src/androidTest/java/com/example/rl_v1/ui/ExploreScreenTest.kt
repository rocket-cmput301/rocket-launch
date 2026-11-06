package com.example.rl_v1.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import com.example.rl_v1.ui.theme.screens.explore.ExploreScreen
import org.junit.Rule
import org.junit.Test

class ExploreScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test fun searchingNarrowsTheCount() {
        composeRule.setContent { ExploreScreen(fakeViewModel(), onSwipeUp = {}, onOpenFilters = {}) }

        composeRule.onNode(hasSetTextAction()).performTextInput("Hinton")

        composeRule.waitUntil(5000) {
            composeRule.onAllNodesWithText("1 campsites").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("1 campsites").assertIsDisplayed()
    }
}
