package com.example.rl_v1.ui.theme.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
 * Purpose: Intermediate screen shown when an existing user profile has `isDeleted == true`,
 * informing the user their account is deactivated and allowing one-tap reactivation.
 * Design Pattern: Jetpack Compose UI state holder backed by DeviceIdProvider and UserRepository.
 * Outstanding Issues: None currently known.
 */

/**
 * Displays a deactivation notice and reactivates the user's Firestore profile (`isDeleted = false`)
 * before invoking [onReactivated].
 */
@Composable
fun DeactivatedAccountScreen(
    onReactivated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val deviceIdProvider = remember { SharedPrefsDeviceIdProvider(context) }
    val userRepo = remember { FirestoreUserRepository() }

    var isReactivating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun reactivateAccount() {
        if (isReactivating) return
        coroutineScope.launch {
            isReactivating = true
            errorMessage = null
            try {
                ensureFirebaseSession()
                val userId = deviceIdProvider.getDeviceId()
                val result = userRepo.reactivateUser(userId)
                result.onSuccess {
                    onReactivated()
                }.onFailure { throwable ->
                    errorMessage = throwable.localizedMessage ?: "Failed to reactivate account."
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Reactivation failed."
            } finally {
                isReactivating = false
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
            text = "Account Deactivated",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Your account on this device is currently deactivated. Reactivate your account to restore access to your profile and continue to the app.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { reactivateAccount() },
            enabled = !isReactivating,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isReactivating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Reactivate Account")
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
    }
}