package com.example.rl_v1.ui.theme.screens.auth

import android.util.Patterns

/** Simple checks for the login and sign-up forms. Returns an error message, or null if OK. */
internal object AuthValidation {
    const val MIN_PASSWORD_LENGTH = 6 // Firebase Auth's minimum

    fun nameError(name: String): String? {
        val trimmed = name.trim()
        return when {
            trimmed.isBlank() -> "Please enter your name."
            !trimmed.any { it.isLetter() } -> "Please enter a valid name."
            else -> null
        }
    }

    fun emailError(email: String): String? = when {
        email.isBlank() -> "Enter your email"
        !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "Enter a valid email"
        else -> null
    }

    fun passwordError(password: String): String? = when {
        password.isEmpty() -> "Enter a password"
        password.length < MIN_PASSWORD_LENGTH ->
            "Password must be at least $MIN_PASSWORD_LENGTH characters"
        else -> null
    }

    fun confirmError(password: String, confirm: String): String? = when {
        confirm.isEmpty() -> "Confirm your password"
        confirm != password -> "Passwords don't match"
        else -> null
    }
}
