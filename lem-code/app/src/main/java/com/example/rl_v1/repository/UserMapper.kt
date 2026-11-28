package com.example.rl_v1.repository

import com.example.rl_v1.model.UserProfile
import com.example.rl_v1.model.UserRole
import com.example.rl_v1.model.UserStatus
import com.google.firebase.firestore.DocumentSnapshot

/**
 * Maps a Firestore [DocumentSnapshot] into a [UserProfile], safely handling missing fields,
 * invalid role strings, and legacy `isDeleted` flags so a single malformed field does not fail
 * profile loading.
 */
fun DocumentSnapshot.toUserProfile(fallbackUserId: String = id): UserProfile {
    val resolvedUserId = getString("userId")?.takeIf { it.isNotBlank() } ?: fallbackUserId

    // Firestore stores arrays as List<*>; convert known enum names safely.
    val rawRoles = get("role") as? List<*>
    val parsedRoles = rawRoles
        ?.mapNotNull { item ->
            val roleName = item as? String ?: return@mapNotNull null
            runCatching { UserRole.valueOf(roleName) }.getOrNull()
        }
        ?.distinct()
        ?.ifEmpty { listOf(UserRole.ENTRANT) }
        ?: listOf(UserRole.ENTRANT)

    val rawStatus = getString("status")
    val parsedStatus = rawStatus
        ?.let { statusName -> runCatching { UserStatus.valueOf(statusName) }.getOrNull() }
        ?: if (getBoolean("isDeleted") == true) {
            UserStatus.DELETED
        } else {
            UserStatus.ACTIVE
        }

    return UserProfile(
        userId = resolvedUserId,
        name = getString("name") ?: "",
        email = getString("email") ?: "",
        phone = getString("phone") ?: "",
        role = parsedRoles,
        photoUrl = getString("photoUrl"),
        notificationsEnabled = getBoolean("notificationsEnabled") ?: true,
        status = parsedStatus
    )
}