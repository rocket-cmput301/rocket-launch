package com.example.rl_v1.repository

import com.example.rl_v1.model.UserProfile
import com.example.rl_v1.model.UserRole
import com.example.rl_v1.model.UserStatus
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreUserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : UserRepository {

    private val usersCollection = firestore.collection("users")

    /**
     * Observes the [UserProfile] for [userId] in real time without creating a document prematurely.
     * Emits `null` when the document does not exist yet.
     */
    override fun observeUser(userId: String): Flow<UserProfile?> = callbackFlow {
        val docRef = usersCollection.document(userId)
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                trySend(snapshot.toUserProfile(fallbackUserId = userId))
            } else {
                trySend(null)
            }
        }
        awaitClose { registration.remove() }
    }

    /**
     * Checks if a user profile exists in Firestore for [userId] without creating one.
     */
    override suspend fun findExistingUser(userId: String): Result<UserProfile?> = runCatching {
        val snapshot = usersCollection.document(userId).get().await()
        if (snapshot.exists()) {
            snapshot.toUserProfile(fallbackUserId = userId)
        } else {
            null
        }
    }

    /**
     * Creates and persists a new [UserProfile] upon completed registration.
     */
    override suspend fun createUserProfile(profile: UserProfile): Result<UserProfile> = runCatching {
        val data = mapOf(
            "userId" to profile.userId,
            "name" to profile.name,
            "email" to profile.email,
            "phone" to profile.phone,
            "role" to profile.role.map { it.name },
            "photoUrl" to profile.photoUrl,
            "notificationsEnabled" to profile.notificationsEnabled,
            "status" to profile.status.name
        )
        usersCollection.document(profile.userId).set(data).await()
        profile
    }

    /**
     * Fetches the [UserProfile] for [userId], including when its [UserStatus] is [UserStatus.DELETED]
     * or [UserStatus.REMOVED].
     * Creates and persists a default entrant profile if no document exists yet.
     */
    override suspend fun getUser(userId: String): Result<UserProfile> = runCatching {
        val docRef = usersCollection.document(userId)
        val snapshot = docRef.get().await()
        if (snapshot.exists()) {
            snapshot.toUserProfile(fallbackUserId = userId)
        } else {
            createDefaultUser(userId)
        }
    }

    /**
     * Updates [UserProfile.name], [UserProfile.email], and [UserProfile.phone] for [userId].
     */
    override suspend fun updateContactInfo(
        userId: String,
        name: String,
        email: String,
        phone: String
    ): Result<Unit> = runCatching {
        usersCollection.document(userId).update(
            mapOf(
                "name" to name,
                "email" to email,
                "phone" to phone
            )
        ).await()
    }

    /**
     * Updates or clears [UserProfile.photoUrl] for [userId].
     */
    override suspend fun updateProfileImage(userId: String, photoUrl: String?): Result<Unit> =
        runCatching {
            usersCollection.document(userId).update("photoUrl", photoUrl).await()
        }

    /**
     * Updates [UserProfile.notificationsEnabled] for [userId].
     */
    override suspend fun setNotificationsEnabled(userId: String, enabled: Boolean): Result<Unit> =
        runCatching {
            usersCollection.document(userId).update("notificationsEnabled", enabled).await()
        }

    override suspend fun setProfileDeleted(userId: String, deleted: Boolean): Result<Unit> =
        runCatching {
            val docRef = usersCollection.document(userId)
            if (deleted) {
                docRef.update("status", UserStatus.DELETED.name).await()
            } else {
                val snapshot = docRef.get().await()
                val currentStatus = if (snapshot.exists()) {
                    snapshot.toUserProfile(fallbackUserId = userId).status
                } else {
                    UserStatus.ACTIVE
                }
                if (currentStatus == UserStatus.REMOVED) {
                    error("Profile has been removed by an administrator and cannot be self-reactivated.")
                }
                docRef.update("status", UserStatus.ACTIVE.name).await()
            }
        }

    /**
     * Sets or revokes admin removal ([UserStatus.REMOVED] vs. [UserStatus.ACTIVE]) for [userId].
     */
    override suspend fun setProfileRemovedByAdmin(userId: String, removed: Boolean): Result<Unit> =
        runCatching {
            val targetStatus = if (removed) UserStatus.REMOVED else UserStatus.ACTIVE
            usersCollection.document(userId).update("status", targetStatus.name).await()
        }

    /**
     * Grants or revokes [role] for [userId] on the `role` array field.
     */
    override suspend fun updateUserRole(
        userId: String,
        role: UserRole,
        granted: Boolean
    ): Result<Unit> = runCatching {
        val updateOp = if (granted) {
            FieldValue.arrayUnion(role.name)
        } else {
            FieldValue.arrayRemove(role.name)
        }
        usersCollection.document(userId).update("role", updateOp).await()
    }

    /**
     * Retrieves all user profiles stored in the `users` collection.
     */
    override suspend fun getAllUsers(): Result<List<UserProfile>> = runCatching {
        val snapshot = usersCollection.get().await()
        snapshot.documents.map { doc -> doc.toUserProfile(fallbackUserId = doc.id) }
    }

    /**
     * Retrieves user profiles matching [status] directly from Firestore.
     */
    override suspend fun getUsersByStatus(status: UserStatus): Result<List<UserProfile>> =
        runCatching {
            val snapshot = usersCollection
                .whereEqualTo("status", status.name)
                .get()
                .await()
            snapshot.documents.map { doc -> doc.toUserProfile(fallbackUserId = doc.id) }
        }

    /** Builds a default entrant [UserProfile] for [userId] without persisting it. */
    private fun buildDefaultUser(userId: String): UserProfile =
        UserProfile(
            userId = userId,
            role = listOf(UserRole.ENTRANT),
            status = UserStatus.ACTIVE
        )

    /** Creates and persists a default entrant [UserProfile] for [userId] in Firestore. */
    private suspend fun createDefaultUser(userId: String): UserProfile {
        val defaultProfile = buildDefaultUser(userId)
        createUserProfile(defaultProfile).getOrThrow()
        return defaultProfile
    }
}