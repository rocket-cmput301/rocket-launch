//package com.example.rl_v1.ui
//
//import androidx.compose.ui.test.assertIsDisplayed
//import androidx.compose.ui.test.junit4.createComposeRule
//import androidx.compose.ui.test.onNodeWithText
//import androidx.compose.ui.test.performClick
//import com.example.rl_v1.ui.theme.screens.profile.ProfileScreen
//import org.junit.Assert.assertTrue
//import org.junit.Rule
//import org.junit.Test
//
//class ProfileScreenTest {
//
//    @get:Rule
//    val composeRule = createComposeRule()
//
//    @Test fun publishButtonWorks() {
//        var clicked = false
//        composeRule.setContent { ProfileScreen(onPublishCampsite = { clicked = true }) }
//
//        composeRule.onNodeWithText("Publish a campsite").performClick()
//
//        assertTrue(clicked)
//    }
//
//    @Test fun policiesOpenAndClose() {
//        composeRule.setContent { ProfileScreen() }
//
//        composeRule.onNodeWithText("App Policies & Guidelines").performClick()
//        composeRule.onNodeWithText("Policies and Guidelines").assertIsDisplayed()
//        composeRule.onNodeWithText("Respect other campers").assertIsDisplayed()
//
//        composeRule.onNodeWithText("Close").performClick()
//        composeRule.onNodeWithText("Policies and Guidelines").assertDoesNotExist()
//    }
//}

package com.example.rl_v1.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.rl_v1.model.UserProfile
import com.example.rl_v1.model.UserRole
import com.example.rl_v1.model.UserStatus
import com.example.rl_v1.ui.theme.screens.profile.ProfileScreen
import com.example.rl_v1.ui.theme.theme.Rlv1Theme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun deleteProfile_cancelKeepsUserOnProfileAndDoesNotTriggerDeleteCallback() {
        var deleteInvoked = false

        composeTestRule.setContent {
            Rlv1Theme {
                ProfileScreen(
                    profile = UserProfile(
                        userId = "user-1",
                        name = "Alex Camper",
                        email = "alex@example.com",
                        role = listOf(UserRole.ENTRANT),
                        status = UserStatus.ACTIVE
                    ),
                    onDeleteProfile = { deleteInvoked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Delete Profile").performClick()
        composeTestRule
            .onNodeWithText("Are you sure you want to delete your profile? You will be signed out and returned to the welcome screen.")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Cancel").performClick()

        assertFalse(deleteInvoked)
        composeTestRule.onNodeWithText("Profile and Preferences").assertIsDisplayed()
    }

    @Test
    fun deleteProfile_confirmInvokesDeleteCallback() {
        var deleteInvoked = false

        composeTestRule.setContent {
            Rlv1Theme {
                ProfileScreen(
                    profile = UserProfile(
                        userId = "user-1",
                        name = "Alex Camper",
                        email = "alex@example.com",
                        role = listOf(UserRole.ENTRANT),
                        status = UserStatus.ACTIVE
                    ),
                    onDeleteProfile = { deleteInvoked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Delete Profile").performClick()
        composeTestRule.onNodeWithText("Delete").performClick()

        assertTrue(deleteInvoked)
    }

    @Test
    fun nonOwnerClickingPublishCampsite_noDismissesPromptAndStaysOnProfile() {
        var publishNavigated = false

        composeTestRule.setContent {
            Rlv1Theme {
                ProfileScreen(
                    profile = UserProfile(
                        userId = "user-1",
                        name = "Alex Camper",
                        email = "alex@example.com",
                        role = listOf(UserRole.ENTRANT),
                        status = UserStatus.ACTIVE
                    ),
                    onPublishCampsite = { publishNavigated = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Publish a campsite").performClick()
        composeTestRule
            .onNodeWithText("To access this you need to be a campsite owner, do you agree?")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("No").performClick()

        assertFalse(publishNavigated)
        composeTestRule.onNodeWithText("Profile and Preferences").assertIsDisplayed()
    }

    @Test
    fun existingOwnerClickingManageCampsites_navigatesImmediatelyWithoutPrompt() {
        var manageNavigated = false
        val destinations = mutableListOf<String>()

        composeTestRule.setContent {
            Rlv1Theme {
                ProfileScreen(
                    profile = UserProfile(
                        userId = "owner-1",
                        name = "Sam Host",
                        email = "sam@example.com",
                        role = listOf(UserRole.ENTRANT, UserRole.OWNER),
                        status = UserStatus.ACTIVE
                    ),
                    onManageCampsites = {
                        manageNavigated = true
                        destinations.add("manage")
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("Manage my campsites").performClick()

        assertTrue(manageNavigated)
        assertEquals(listOf("manage"), destinations)
    }
}
