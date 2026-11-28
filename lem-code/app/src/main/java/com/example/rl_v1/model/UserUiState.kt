package com.example.rl_v1.model

/**
 * Represents the current state of the user profile.
 * @property isLoading `true` while the profile is being loaded.
 * @property profile The current user profile, if any.
 * @property errorMessage Any error message that occurred during profile loading.
 */
data class UserUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile? = null,
    val errorMessage: String? = null
)
