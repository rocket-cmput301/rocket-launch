package com.example.rl_v1.ui.theme.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rl_v1.ui.theme.components.ClickablePrimaryTextBox
import com.example.rl_v1.ui.theme.components.Header1
import com.example.rl_v1.ui.theme.components.LargeButtonPrimary
import com.example.rl_v1.ui.theme.components.LargeButtonSecondary
import com.example.rl_v1.ui.theme.components.ScreenHeader
import com.example.rl_v1.ui.theme.theme.Rlv1Theme

/**
 * Admin hub tab for moderation, user/owner management, and system log review.
 * @param totalProfilesCount The number of registered users and owners.
 * @param onManageProfiles Callback for when the "Manage profiles" button is clicked.
 * @param onManageCampsites Callback for when the "Browse & remove campsites" button is clicked.
 * @param onManageImages Callback for when the "Inspect uploaded campsite & profile images" button is clicked.
 * @param onModerateReviewsAndRatings Callback for when the "Remove policy-violating ratings & reviews" button is clicked.
 * @param onViewNotificationLogs Callback for when the "Review all sent notifications" button is clicked.
 * @param onViewMessageLogs Callback for when the "Review owner-to-entrant reservation messages" button is clicked.
 * @param modifier Modifier for styling.
 */
@Composable
fun AdminScreen(
    totalProfilesCount: Int? = null,
    onManageProfiles: () -> Unit = {},
    onManageCampsites: () -> Unit = {},
    onManageImages: () -> Unit = {},
    onModerateReviewsAndRatings: () -> Unit = {},
    onViewNotificationLogs: () -> Unit = {},
    onViewMessageLogs: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val profilesSummary = totalProfilesCount?.let { "$it registered profiles" }
        ?: "Browse and remove user or owner profiles"

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 16.dp)
    ) {
        item {
            ScreenHeader("Admin Console")
        }

        item {
            Header1("Users & Moderation")
        }

        item {
            LargeButtonPrimary(
                text = "Manage profiles",
                onClick = onManageProfiles
            )
        }

        item {
            ClickablePrimaryTextBox(
                value = "$profilesSummary >",
                label = "User & Owner Profiles",
                onClick = onManageProfiles,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            LargeButtonSecondary(
                text = "Browse & remove campsites",
                onClick = onManageCampsites
            )
        }

        item {
            ClickablePrimaryTextBox(
                value = "Inspect uploaded campsite & profile images >",
                label = "Uploaded Images",
                onClick = onManageImages,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            ClickablePrimaryTextBox(
                value = "Remove policy-violating ratings & reviews >",
                label = "Ratings & Reviews",
                onClick = onModerateReviewsAndRatings,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Header1("Activity & Audit Logs")
        }

        item {
            ClickablePrimaryTextBox(
                value = "Review all sent notifications >",
                label = "Notification Logs",
                onClick = onViewNotificationLogs,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            ClickablePrimaryTextBox(
                value = "Review owner-to-entrant reservation messages >",
                label = "Message Logs",
                onClick = onViewMessageLogs,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun AdminScreenPreview() {
    Rlv1Theme(dynamicColor = false) {
        AdminScreen(totalProfilesCount = 24)
    }
}