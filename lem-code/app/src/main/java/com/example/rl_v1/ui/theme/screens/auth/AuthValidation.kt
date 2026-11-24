package com.example.rl_v1.ui.theme.screens.auth

import android.util.Patterns

/** Simple form checks. Returns an error message, or null if OK. */
internal object AuthValidation {
    fun emailError(email: String): String? = when {
        email.isBlank() -> "Enter your email"
        !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "Enter a valid email"
        else -> null
    }
}
