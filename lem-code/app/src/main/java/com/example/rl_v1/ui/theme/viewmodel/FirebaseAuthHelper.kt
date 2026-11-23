package com.example.rl_v1.ui.theme.viewmodel

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * Ensures Firebase Authentication has an active session (signing in anonymously if needed)
 * so Firestore rules checking `request.auth != null` succeed.
 *
 * @return The active Firebase Auth UID (for session/rule verification), while `UserProfile.userId`
 * remains the device identifier.
 */
suspend fun ensureFirebaseSession(
    auth: FirebaseAuth = FirebaseAuth.getInstance()
): String {
    auth.currentUser?.uid?.let { return it }
    val result = auth.signInAnonymously().await()
    return result.user?.uid ?: error("Firebase anonymous sign-in did not return a user.")
}