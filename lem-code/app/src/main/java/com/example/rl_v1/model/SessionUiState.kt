package com.example.rl_v1.model

/**
 * Represents the current session state for the application.
 * @property isLoading `true` while the session is being initialized.
 * @property userId The device-based user id.
 * @property profile The current user profile, if any.
 * @property activeRole The current user role.
 * @property errorMessage Any error message that occurred during session initialization.
 */
data class SessionUiState(
    val isLoading: Boolean = true,
    val userId: String = "",
    val profile: UserProfile? = null,
    val activeRole: UserRole? = UserRole.ENTRANT,
    val errorMessage: String? = null
)