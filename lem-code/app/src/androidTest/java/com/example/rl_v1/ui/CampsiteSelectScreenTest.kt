package com.example.rl_v1.ui

import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.example.rl_v1.ui.theme.screens.explore.CampsiteSelectScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CampsiteSelectScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test fun tappingACampsiteGivesItsId() {
        var clickedId = ""
        composeRule.setContent {
            CampsiteSelectScreen(fakeViewModel(), onBack = {}, onCampsiteClick = { id -> clickedId = id })
        }
        composeRule.waitUntil(5000) {
            composeRule.onAllNodesWithText("Lake View").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("Lake View"))
        composeRule.onNodeWithText("Lake View").performClick()

        assertEquals("lake", clickedId)
    }
}
