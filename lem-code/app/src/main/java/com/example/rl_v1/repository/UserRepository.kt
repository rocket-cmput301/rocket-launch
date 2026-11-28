package com.example.rl_v1.repository

import com.example.rl_v1.model.UserProfile
import com.example.rl_v1.model.UserRole
import com.example.rl_v1.model.UserStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository abstraction for user profile, role, status, and notification preference management
 * using the device identifier as the user ID.
 */
interface UserRepository {
    /**
     * Observes the user profile for [userId] in real time without creating a document.
     * Emits `null` if no profile document exists yet in Firestore.
     */
    fun observeUser(userId: String): Flow<UserProfile?>

    /**
     * Checks if a user profile document exists for [userId] without creating one.
     * Returns `null` if the user has not completed registration yet.
     */
    suspend fun findExistingUser(userId: String): Result<UserProfile?>

    /**
     * Creates and persists a new [UserProfile] in Firestore upon completed registration.
     */
    suspend fun createUserProfile(profile: UserProfile): Result<UserProfile>

    /**
     * Retrieves the user profile for [userId], including when its [UserStatus] is [UserStatus.DELETED]
     * or [UserStatus.REMOVED].
     * If no profile document exists yet, creates and returns a default entrant profile.
     */
    suspend fun getUser(userId: String): Result<UserProfile>

    /**
     * Updates the contact details for [userId].
     */
    suspend fun updateContactInfo(
        userId: String,
        name: String,
        email: String,
        phone: String
    ): Result<Unit>

    /**
     * Updates or clears the profile image URL for [userId].
     */
    suspend fun updateProfileImage(userId: String, photoUrl: String?): Result<Unit>

    /**
     * Updates whether notifications are enabled for [userId].
     */
    suspend fun setNotificationsEnabled(userId: String, enabled: Boolean): Result<Unit>

    /**
     * Marks the profile for [userId] as self-deleted ([UserStatus.DELETED]) when [deleted] is `true`,
     * or restores a [UserStatus.DELETED] profile to [UserStatus.ACTIVE] when [deleted] is `false`.
     * Does not reactivate profiles with [UserStatus.REMOVED].
     */
    suspend fun setProfileDeleted(userId: String, deleted: Boolean): Result<Unit>

    /**
     * Sets or revokes admin removal ([UserStatus.REMOVED] vs. [UserStatus.ACTIVE]) for [userId].
     */
    suspend fun setProfileRemovedByAdmin(userId: String, removed: Boolean): Result<Unit>

    /**
     * Grants or revokes a [role] for [userId].
     */
    suspend fun updateUserRole(userId: String, role: UserRole, granted: Boolean): Result<Unit>

    /**
     * Retrieves all user profiles in the system.
     */
    suspend fun getAllUsers(): Result<List<UserProfile>>

    /**
     * Retrieves user profiles matching the given [status].
     */
    suspend fun getUsersByStatus(status: UserStatus): Result<List<UserProfile>>
}