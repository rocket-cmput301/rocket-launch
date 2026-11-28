package com.example.rl_v1.ui.theme.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.rl_v1.model.SharedPrefsDeviceIdProvider
import com.example.rl_v1.model.UserProfile
import com.example.rl_v1.model.UserRole
import com.example.rl_v1.repository.FirestoreUserRepository
import com.example.rl_v1.ui.theme.components.ClickablePrimaryTextBox
import com.example.rl_v1.ui.theme.components.Header1
import com.example.rl_v1.ui.theme.components.LargeButtonPrimary
import com.example.rl_v1.ui.theme.components.LargeButtonSecondary
import com.example.rl_v1.ui.theme.components.PoliciesDialog
import com.example.rl_v1.ui.theme.components.PrimaryClickableText
import com.example.rl_v1.ui.theme.components.ScreenHeader
import com.example.rl_v1.ui.theme.theme.Forest
import com.example.rl_v1.ui.theme.viewmodel.ensureFirebaseSession
import kotlinx.coroutines.launch

private enum class HostingDestination {
    PUBLISH,
    MANAGE
}

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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val deviceIdProvider = remember { SharedPrefsDeviceIdProvider(context) }
    val userRepo = remember { FirestoreUserRepository() }

    var showPolicies by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var pendingHostingDestination by rememberSaveable { mutableStateOf<HostingDestination?>(null) }
    var isGrantingOwnerRole by remember { mutableStateOf(false) }
    var ownerPromptError by remember { mutableStateOf<String?>(null) }

    val isCampsiteOwner = profile?.role?.contains(UserRole.OWNER) == true
    val isAnyDialogOpen = showPolicies || showDeleteConfirmDialog || pendingHostingDestination != null

    val displayName = profile?.name?.trim()?.takeIf { it.isNotEmpty() } ?: "Add your name"
    val displayEmail = profile?.email?.trim()?.takeIf { it.isNotEmpty() } ?: "Add your email"
    val notificationsLabel = if (profile?.notificationsEnabled != false) {
        "Notifications: On"
    } else {
        "Notifications: Off"
    }

    fun navigateToHostingDestination(destination: HostingDestination) {
        when (destination) {
            HostingDestination.PUBLISH -> onPublishCampsite()
            HostingDestination.MANAGE -> onManageCampsites()
        }
    }

    fun handleHostingActionClick(destination: HostingDestination) {
        if (isCampsiteOwner) {
            navigateToHostingDestination(destination)
        } else {
            ownerPromptError = null
            pendingHostingDestination = destination
        }
    }

    fun confirmOwnerRoleAndNavigate(destination: HostingDestination) {
        if (isGrantingOwnerRole) return
        coroutineScope.launch {
            isGrantingOwnerRole = true
            ownerPromptError = null
            try {
                ensureFirebaseSession()
                val userId = deviceIdProvider.getDeviceId()
                val result = userRepo.updateUserRole(
                    userId = userId,
                    role = UserRole.OWNER,
                    granted = true
                )
                result.onSuccess {
                    pendingHostingDestination = null
                    navigateToHostingDestination(destination)
                }.onFailure { throwable ->
                    ownerPromptError =
                        throwable.localizedMessage ?: "Failed to assign campsite owner role."
                }
            } catch (e: Exception) {
                ownerPromptError =
                    e.localizedMessage ?: "Failed to assign campsite owner role."
            } finally {
                isGrantingOwnerRole = false
            }
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            // Blur the profile while a dialog is open
            .then(if (isAnyDialogOpen) Modifier.blur(12.dp) else Modifier)
            .padding(top = 16.dp)
    ) {
        item {
            ScreenHeader("Profile and Preferences")
        }
        item {
            Header1("Hosting")
        }
        item {
            LargeButtonPrimary(
                "Publish a campsite",
                onClick = { handleHostingActionClick(HostingDestination.PUBLISH) }
            )
        }
        item {
            LargeButtonSecondary(
                "Manage my campsites",
                onClick = { handleHostingActionClick(HostingDestination.MANAGE) }
            )
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
                onClick = { showDeleteConfirmDialog = true },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    if (showPolicies) {
        PoliciesDialog(onDismiss = { showPolicies = false })
    }

    pendingHostingDestination?.let { destination ->
        ThemedActionDialog(
            title = "Campsite Owner Access",
            message = "To access this you need to be a campsite owner, do you agree?",
            confirmLabel = "Yes",
            dismissLabel = "No",
            confirmColor = Forest,
            isLoading = isGrantingOwnerRole,
            errorMessage = ownerPromptError,
            onConfirm = { confirmOwnerRoleAndNavigate(destination) },
            onDismiss = {
                if (!isGrantingOwnerRole) {
                    ownerPromptError = null
                    pendingHostingDestination = null
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        ThemedActionDialog(
            title = "Delete Profile",
            message = "Are you sure you want to delete your profile? You will be signed out and returned to the welcome screen.",
            confirmLabel = "Delete",
            dismissLabel = "Cancel",
            confirmColor = Color(0xFFC4532D),
            onConfirm = {
                showDeleteConfirmDialog = false
                onDeleteProfile()
            },
            onDismiss = { showDeleteConfirmDialog = false }
        )
    }
}

@Composable
private fun ThemedActionDialog(
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    confirmColor: Color,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Forest
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF222222)
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC4532D)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text(
                            text = dismissLabel,
                            color = Forest
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = onConfirm,
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = confirmColor,
                            contentColor = Color.White
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(text = confirmLabel)
                        }
                    }
                }
            }
        }
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