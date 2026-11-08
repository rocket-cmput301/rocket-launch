package com.example.rl_v1.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.example.rl_v1.model.Campsite
import com.example.rl_v1.ui.theme.screens.CampsiteEditScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CampsiteEditScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test fun savingGivesTheNewName() {
        var saved: Campsite? = null
        composeRule.setContent { CampsiteEditScreen(hintonPines, onSave = { saved = it }, onCancel = {}) }

        composeRule.onNodeWithText("Name").performTextClearance()
        composeRule.onNodeWithText("Name").performTextInput("New Name")
        composeRule.onNodeWithText("Save").performScrollTo().performClick()

        assertEquals("New Name", saved?.name)
    }
}
