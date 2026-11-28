package com.example.rl_v1.ui.theme.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.rl_v1.model.SharedPrefsDeviceIdProvider
import com.example.rl_v1.model.UserProfile
import com.example.rl_v1.model.UserRole
import com.example.rl_v1.model.UserStatus
import com.example.rl_v1.repository.FirestoreUserRepository
import com.example.rl_v1.ui.theme.viewmodel.ensureFirebaseSession
import kotlinx.coroutines.launch

/*
- Registration screen for first-time users displaying the generated device user ID,
- collecting contact information (name, email, phone), and allowing role selection
- (locked ENTRANT + optional OWNER; ADMIN excluded). Writes to Firestore only on submit.
 */

/**
 * Displays initial registration inputs and persists a new [UserProfile] in Firestore
 * only when the user completes registration.
 */
@Composable
fun RegistrationScreen(
    onRegistrationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val deviceIdProvider = remember { SharedPrefsDeviceIdProvider(context) }
    val userRepo = remember { FirestoreUserRepository() }

    val generatedUserId = remember { deviceIdProvider.getDeviceId() }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isOwnerSelected by remember { mutableStateOf(false) }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun submitRegistration() {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()
        val trimmedPhone = phone.trim()

        if (trimmedName.isBlank()) {
            errorMessage = "Please enter your name."
            return
        }
        val emailValidationError = AuthValidation.emailError(trimmedEmail)
        if (emailValidationError != null) {
            errorMessage = emailValidationError
            return
        }

        val selectedRoles = buildList {
            add(UserRole.ENTRANT)
            if (isOwnerSelected) {
                add(UserRole.OWNER)
            }
        }

        val newProfile = UserProfile(
            userId = generatedUserId,
            name = trimmedName,
            email = trimmedEmail,
            phone = trimmedPhone,
            role = selectedRoles,
            notificationsEnabled = true,
            status = UserStatus.ACTIVE
        )

        coroutineScope.launch {
            isSubmitting = true
            errorMessage = null
            try {
                ensureFirebaseSession()
                val createResult = userRepo.createUserProfile(newProfile)
                createResult.onSuccess {
                    onRegistrationComplete()
                }.onFailure { throwable ->
                    errorMessage = throwable.localizedMessage ?: "Failed to complete registration."
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Registration failed."
            } finally {
                isSubmitting = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = "Complete Registration",
            style = MaterialTheme.typography.headlineMedium
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Generated User ID",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = generatedUserId,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                errorMessage = null
            },
            label = { Text("Full Name") },
            singleLine = true,
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            label = { Text("Email Address") },
            singleLine = true,
            enabled = !isSubmitting,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = phone,
            onValueChange = {
                phone = it
                errorMessage = null
            },
            label = { Text("Phone Number (Optional)") },
            singleLine = true,
            enabled = !isSubmitting,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Select Your Roles",
            style = MaterialTheme.typography.titleSmall
        )

        // Entrant role is required and locked on
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = true,
                onCheckedChange = null,
                enabled = false
            )
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text("Entrant (Required)", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Browse campsites and join reservation lotteries.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Campsite Owner role is optional and toggleable
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isSubmitting) { isOwnerSelected = !isOwnerSelected }
        ) {
            Checkbox(
                checked = isOwnerSelected,
                onCheckedChange = { isOwnerSelected = it },
                enabled = !isSubmitting
            )
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text("Campsite Owner", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Publish and manage your own campsites.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = { submitRegistration() },
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Complete Registration")
            }
        }
    }
}