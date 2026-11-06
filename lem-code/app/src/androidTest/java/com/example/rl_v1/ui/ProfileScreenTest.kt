package com.example.rl_v1.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.rl_v1.ui.theme.screens.profile.ProfileScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfileScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test fun publishButtonWorks() {
        var clicked = false
        composeRule.setContent { ProfileScreen(onPublishCampsite = { clicked = true }) }

        composeRule.onNodeWithText("Publish a campsite").performClick()

        assertTrue(clicked)
    }
}
