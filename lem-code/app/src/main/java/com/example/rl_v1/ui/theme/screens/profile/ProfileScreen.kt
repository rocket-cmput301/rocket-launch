package com.example.rl_v1.ui.theme.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rl_v1.model.UserProfile
import com.example.rl_v1.ui.theme.components.ClickablePrimaryTextBox
import com.example.rl_v1.ui.theme.components.Header1
import com.example.rl_v1.ui.theme.components.LargeButtonPrimary
import com.example.rl_v1.ui.theme.components.LargeButtonSecondary
import com.example.rl_v1.ui.theme.components.PoliciesDialog
import com.example.rl_v1.ui.theme.components.PrimaryClickableText
import com.example.rl_v1.ui.theme.components.ScreenHeader
import com.example.rl_v1.ui.theme.theme.Forest

/** Profile tab, with buttons to publish and manage campsites and edit profile settings. */
@Composable
fun ProfileScreen(
    profile: UserProfile? = null,
    onPublishCampsite: () -> Unit = {},
    onManageCampsites: () -> Unit = {},
    onEditProfilePicture: () -> Unit = {},
    onEditPersonalInformation: () -> Unit = {},
    onEditPreferences: () -> Unit = {}, // TODO: no preferences screen yet
    onEditAccessibility: () -> Unit = {},
    onSaveProfile: () -> Unit = {},
    onDeleteProfile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showPolicies by rememberSaveable { mutableStateOf(false) }

    val displayName = profile?.name?.trim()?.takeIf { it.isNotEmpty() } ?: "Add your name"
    val displayEmail = profile?.email?.trim()?.takeIf { it.isNotEmpty() } ?: "Add your email"
    val notificationsLabel = if (profile?.notificationsEnabled != false) {
        "Notifications: On"
    } else {
        "Notifications: Off"
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            // Blur the profile while the policies popup is open
            .then(if (showPolicies) Modifier.blur(12.dp) else Modifier)
            .padding(top = 16.dp)
    ) {
        item {
            ScreenHeader("Profile and Preferences")
        }
        item {
            Header1("Hosting")
        }
        item {
            LargeButtonPrimary("Publish a campsite", onClick = onPublishCampsite)
        }
        item {
            LargeButtonSecondary("Manage my campsites", onClick = onManageCampsites)
        }
        item {
            ClickablePrimaryTextBox(
                value = "Change Profile photo >",
                label = "Profile Photo",
                onClick = onEditProfilePicture,
                modifier = Modifier.fillMaxSize(),
            )
        }
        item {
            ClickablePrimaryTextBox(
                value = "$displayName\n$displayEmail",
                label = "Personal Information",
                onClick = onEditPersonalInformation,
                modifier = Modifier.fillMaxSize(),
            )
        }
        item {
            ClickablePrimaryTextBox(
                value = "\"Forest Hiking Lakeside\nPersonalizes featured campsites\"",
                label = "Your Outdoor Preferences",
                onClick = onEditPreferences,
                modifier = Modifier.fillMaxSize(),
            )
        }
        item {
            ClickablePrimaryTextBox(
                value = "Accessibility Settings >\n$notificationsLabel",
                label = "Make it yours",
                onClick = onEditAccessibility,
                modifier = Modifier.fillMaxSize(),
            )
        }
        item {
            PrimaryClickableText(
                text = "App Policies & Guidelines",
                textColor = Forest,
                onClick = { showPolicies = true },
                modifier = Modifier.fillMaxSize(),
            )
        }
        item {
            LargeButtonPrimary("Save Profile", onClick = onSaveProfile)
        }
        item {
            PrimaryClickableText(
                text = "Delete Profile",
                textColor = Color(0xFFC4532D),
                onClick = onDeleteProfile,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    if (showPolicies) {
        PoliciesDialog(onDismiss = { showPolicies = false })
    }
}

@Preview
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(
        profile = UserProfile(
            name = "Sample Camper",
            email = "camper@example.com",
            notificationsEnabled = true
        )
    )
}
