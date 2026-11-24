package com.example.rl_v1.ui.theme.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.rl_v1.model.SharedPrefsDeviceIdProvider
import com.example.rl_v1.repository.FirestoreUserRepository
import com.example.rl_v1.ui.theme.viewmodel.ensureFirebaseSession
import kotlinx.coroutines.launch

/*
 * Purpose: Entry welcome screen with a single "Get Started" action that checks whether
 * the current device has an existing Firestore user profile without creating one prematurely,
 * plus temporary debug controls to inspect, override, or reset the persisted device ID.
 * Design Pattern: Jetpack Compose UI state holder coordinating DeviceIdProvider and UserRepository.
 * Outstanding Issues: Remove the debug Device ID card before production release.
 */

@Composable
fun WelcomeScreen(
    onNeedsRegistration: () -> Unit,
    onAccountDeactivated: () -> Unit,
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val deviceIdProvider = remember { SharedPrefsDeviceIdProvider(context) }
    val userRepo = remember { FirestoreUserRepository() }

    var activeDeviceId by remember { mutableStateOf(deviceIdProvider.getDeviceId()) }
    var customDeviceIdInput by remember { mutableStateOf("") }
    var debugStatusMessage by remember { mutableStateOf<String?>(null) }

    var isChecking by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun handleGetStarted() {
        if (isChecking) return
        coroutineScope.launch {
            isChecking = true
            errorMessage = null
            try {
                ensureFirebaseSession()
                val userId = deviceIdProvider.getDeviceId()
                activeDeviceId = userId
                val result = userRepo.findExistingUser(userId)

                result.onSuccess { existingProfile ->
                    when {
                        existingProfile == null -> onNeedsRegistration()
                        existingProfile.isDeleted -> onAccountDeactivated()
                        else -> onAuthenticated()
                    }
                }.onFailure { throwable ->
                    errorMessage = throwable.localizedMessage ?: "Failed to check account status."
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Failed to initialize session."
            } finally {
                isChecking = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = "Welcome to Rocket Launch!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Discover campsites, join lotteries, and manage your reservations.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { handleGetStarted() },
            enabled = !isChecking,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isChecking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(text = "Get Started")
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Debug / Testing Card for switching or resetting Device ID
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Testing: Device ID",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Current ID: $activeDeviceId",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = customDeviceIdInput,
                    onValueChange = {
                        customDeviceIdInput = it
                        debugStatusMessage = null
                    },
                    label = { Text("Custom Device ID") },
                    singleLine = true,
                    enabled = !isChecking,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val trimmed = customDeviceIdInput.trim()
                            if (trimmed.isNotEmpty()) {
                                deviceIdProvider.setDeviceId(trimmed)
                                activeDeviceId = deviceIdProvider.getDeviceId()
                                customDeviceIdInput = ""
                                debugStatusMessage = "Device ID updated."
                            } else {
                                debugStatusMessage = "Enter a non-empty ID first."
                            }
                        },
                        enabled = !isChecking,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Set ID")
                    }

                    OutlinedButton(
                        onClick = {
                            deviceIdProvider.resetDeviceId()
                            activeDeviceId = deviceIdProvider.getDeviceId()
                            customDeviceIdInput = ""
                            debugStatusMessage = "Generated new Device ID."
                        },
                        enabled = !isChecking,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("New ID")
                    }
                }

                if (debugStatusMessage != null) {
                    Text(
                        text = debugStatusMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}