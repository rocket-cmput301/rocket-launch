package com.example.rl_v1.ui.theme.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.rl_v1.model.SharedPrefsDeviceIdProvider
import com.example.rl_v1.model.UserProfile
import com.example.rl_v1.model.UserRole
import com.example.rl_v1.repository.FirestoreUserRepository
import com.example.rl_v1.ui.theme.viewmodel.ensureFirebaseSession
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * Purpose: Demo screen for testing Firebase anonymous auth, device ID (`userId`)
 * profile creation in Firestore, and role updates.
 * Design Pattern: Compose UI state holder coordinating [FirebaseAuth],
 * [SharedPrefsDeviceIdProvider], and [FirestoreUserRepository].
 * Outstanding Issues: Move auth/session initialization into a ViewModel for production screens.
 */
@Composable
fun TempLoginScreen(
    onContinueToApp: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val auth = remember { FirebaseAuth.getInstance() }
    val userRepo = remember { FirestoreUserRepository() }
    val deviceIdProvider = remember { SharedPrefsDeviceIdProvider(context) }

    LaunchedEffect(Unit) {
        try {
            ensureFirebaseSession(auth)
            val deviceId = deviceIdProvider.getDeviceId()
            val result = userRepo.getUser(deviceId)
            profile = result.getOrNull()
            if (profile == null) {
                errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to load user profile"
            }
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: "Failed to initialize session"
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text("Profile & Auth Demo", style = MaterialTheme.typography.headlineMedium)

        if (loading) {
            CircularProgressIndicator()
            Text("Initializing Firebase session and user profile...")
        } else if (errorMessage != null) {
            Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
        } else if (profile != null) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("User ID (Device ID): ${profile!!.userId}", style = MaterialTheme.typography.bodySmall)
                    Text("Name: ${profile!!.name.ifBlank { "N/A" }}", style = MaterialTheme.typography.bodyLarge)
                    Text("Email: ${profile!!.email.ifBlank { "N/A" }}", style = MaterialTheme.typography.bodyMedium)
                    Text("Roles: ${profile!!.role.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Button(
                onClick = {
                    val current = profile ?: return@Button
                    if (!current.role.contains(UserRole.OWNER)) {
                        coroutineScope.launch {
                            userRepo.updateUserRole(current.userId, UserRole.OWNER, true)
                            val updated = userRepo.getUser(current.userId).getOrNull()
                            if (updated != null) {
                                profile = updated
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Upgrade to Campsite Owner Role")
            }

            Button(
                onClick = onContinueToApp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue to Main App")
            }
        }
    }
}