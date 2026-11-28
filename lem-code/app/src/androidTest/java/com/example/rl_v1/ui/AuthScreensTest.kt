package com.example.rl_v1.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.rl_v1.ui.theme.screens.auth.DeactivatedAccountScreen
import com.example.rl_v1.ui.theme.screens.auth.RegistrationScreen
import com.example.rl_v1.ui.theme.screens.auth.WelcomeScreen
import com.example.rl_v1.ui.theme.theme.Rlv1Theme
import org.junit.Rule
import org.junit.Test

class AuthScreensTest {

    @get:Rule
    val composeTestRule = createComposeRule()

//    @Test
//    fun registrationScreen_showsValidationErrorWhenNameIsBlank() {
//        composeTestRule.setContent {
//            Rlv1Theme {
//                RegistrationScreen(onRegistrationComplete = {})
//            }
//        }
//
//        composeTestRule.onNodeWithText("Email Address").performTextInput("camper@example.com")
//        composeTestRule.onNodeWithText("Complete Registration").performClick()
//
//        composeTestRule.onNodeWithText("Please enter your name.").assertIsDisplayed()
//    }

    @Test
    fun registrationScreen_displaysLockedEntrantAndOptionalOwnerRole() {
        composeTestRule.setContent {
            Rlv1Theme {
                RegistrationScreen(onRegistrationComplete = {})
            }
        }

        composeTestRule.onNodeWithText("Entrant (Required)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Campsite Owner").assertIsDisplayed()
    }

    @Test
    fun welcomeScreen_displaysGetStartedAndDeviceIdControls() {
        composeTestRule.setContent {
            Rlv1Theme {
                WelcomeScreen(
                    onNeedsRegistration = {},
                    onAccountDeactivated = {},
                    onAuthenticated = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Welcome to Rocket Launch!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Get Started").assertIsDisplayed()
        composeTestRule.onNodeWithText("Testing: Device ID").assertIsDisplayed()
    }

    @Test
    fun deactivatedAccountScreen_displaysDeactivatedNoticeAndReactivateButton() {
        composeTestRule.setContent {
            Rlv1Theme {
                DeactivatedAccountScreen(onReactivated = {})
            }
        }

        composeTestRule.onNodeWithText("Account Deactivated").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reactivate Account").assertIsDisplayed()
    }
}