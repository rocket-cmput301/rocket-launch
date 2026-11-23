package com.example.rl_v1.model

/**
 * Represents the current device-based login/session state for the application.
 */
data class SessionUiState(
    val isLoading: Boolean = true,
    val userId: String = "",
    val profile: UserProfile? = null,
    val activeRole: UserRole = UserRole.ENTRANT,
    val errorMessage: String? = null
)