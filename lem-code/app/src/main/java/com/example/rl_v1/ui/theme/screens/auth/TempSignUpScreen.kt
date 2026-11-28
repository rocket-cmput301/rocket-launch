package com.example.rl_v1.ui.theme.screens.auth

import com.example.rl_v1.ui.theme.viewmodel.ensureFirebaseSession
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.rl_v1.repository.FirestoreUserRepository
import kotlinx.coroutines.launch

/*
 * Purpose: Sign-up screen allowing new users to enter their contact details
 * (name, email, phone) while creating/updating their device-identified Firestore profile.
 * Design Pattern: Jetpack Compose UI screen backed by DeviceIdProvider and FirestoreUserRepository.
 * Outstanding Issues: Consider moving form validation and submission into an AuthViewModel later.
 */

/**
 * Displays contact detail fields for initial sign-up, persists the profile for the current
 * device ID (`userId`), and invokes [onSignedUp] upon completion.
 *
 * @param onSignedUp Callback invoked after the user profile is created and saved.
 * @param onGoToLogin Callback to navigate back to the login screen.
 */
@Composable
fun TempSignUpScreen(
    onSignedUp: () -> Unit,
    onGoToLogin: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val deviceIdProvider = remember { SharedPrefsDeviceIdProvider(context) }
    val userRepo = remember { FirestoreUserRepository() }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun submitSignUp() {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()
        val trimmedPhone = phone.trim()

        if (trimmedName.isBlank()) {
            errorMessage = "Please enter your name."
            return
        }
        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            errorMessage = "Please enter a valid email address."
            return
        }

        coroutineScope.launch {
            isSubmitting = true
            errorMessage = null
            try {
                ensureFirebaseSession()
                val userId = deviceIdProvider.getDeviceId()

                // Ensure the user document exists first (creates default ENTRANT profile if missing)
                val fetchResult = userRepo.getUser(userId)
                if (fetchResult.isFailure) {
                    errorMessage = fetchResult.exceptionOrNull()?.localizedMessage
                        ?: "Failed to initialize user profile."
                    return@launch
                }

                // Persist the entered contact details
                val updateResult = userRepo.updateContactInfo(
                    userId = userId,
                    name = trimmedName,
                    email = trimmedEmail,
                    phone = trimmedPhone
                )
                updateResult.onSuccess {
                    onSignedUp()
                }.onFailure { throwable ->
                    errorMessage = throwable.localizedMessage ?: "Failed to save contact details."
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Sign-up failed."
            } finally {
                isSubmitting = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Enter your contact details to get started on this device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

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

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = { submitSignUp() },
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Sign Up")
            }
        }

        TextButton(
            onClick = onGoToLogin,
            enabled = !isSubmitting
        ) {
            Text("Already set up on this device? Log In")
        }
    }
}