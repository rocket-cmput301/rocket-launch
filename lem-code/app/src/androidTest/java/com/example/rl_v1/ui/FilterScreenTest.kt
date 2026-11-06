package com.example.rl_v1.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.rl_v1.ui.theme.screens.explore.MakeFilterScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FilterScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test fun pickAPreference() {
        var preferences = ArrayList<String>()
        composeRule.setContent {
            MakeFilterScreen(confirmFilters = { _, _, _, p, _ -> preferences = p })
        }

        composeRule.onNodeWithText("Forest").performClick()
        composeRule.onNodeWithText("Show Campsites").performClick()

        assertEquals(1, preferences.size)
        assertEquals("Forest", preferences[0])
    }
}
