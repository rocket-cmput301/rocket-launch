package com.example.rl_v1.model

/**
 * User Profile Class to hold and apply all relevant details to a User
 *
 * @property userId Unique User ID based on their device ID
 * @property name Name of the User
 * @property email Email of the User
 * @property phone Phone number of the User
 * @property role Roles of the User
 * @property photoUrl URL of the User's profile picture
 * @property notificationsEnabled Whether the User wants to receive notifications
 * @property status User account status
 */
data class UserProfile (
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: List<UserRole> = mutableListOf<UserRole>(UserRole.ENTRANT),
    val photoUrl: String? = null,
    val notificationsEnabled: Boolean = true,
    val status: UserStatus = UserStatus.ACTIVE
) {
    /** Returns true if the user has been granted [targetRole]. */
    fun hasRole(targetRole: UserRole): Boolean = role.contains(targetRole)
}