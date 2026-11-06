package com.example.rl_v1.ui

import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import com.example.rl_v1.ui.theme.screens.CampsiteRegistrationScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CampsiteRegistrationScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    /** Scrolls to the box called [label] and types [text] into it. */
    private fun typeInto(label: String, text: String) {
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText(label))
        composeRule.onNode(hasSetTextAction() and hasAnySibling(hasText(label)), useUnmergedTree = true)
            .performTextInput(text)
    }

    private fun tapPublish() {
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("Publish Campsite"))
        composeRule.onNodeWithText("Publish Campsite").performClick()
    }

    @Test fun emptyFormAsksForAName() {
        composeRule.setContent { CampsiteRegistrationScreen(fakeViewModel(), onBack = {}, onDone = {}) }

        tapPublish()

        composeRule.onNodeWithText("Enter a campsite name").assertExists()
    }

    @Test fun fullFormPublishes() {
        val campsites = FakeCampsiteRepository(emptyList())
        var done = false
        composeRule.setContent {
            CampsiteRegistrationScreen(fakeViewModel(campsites = campsites), onBack = {}, onDone = { done = true })
        }

        typeInto("Name", "Test Camp")
        typeInto("Location", "Jasper, AB")
        typeInto("Maximum guests", "4")
        typeInto("Nightly price (CAD)", "50")
        tapPublish()

        composeRule.waitUntil(5000) { done }
        assertEquals(1, campsites.campsites.value.size)
        assertEquals("Test Camp", campsites.campsites.value[0].name)
        assertTrue(done)
    }
}
