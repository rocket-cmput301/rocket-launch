package com.example.rl_v1.ui.theme.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rl_v1.model.SharedPrefsDeviceIdProvider
import com.example.rl_v1.model.UserProfile
import com.example.rl_v1.model.UserRole
import com.example.rl_v1.model.UserStatus
import com.example.rl_v1.model.UserUiState
import com.example.rl_v1.repository.FirestoreUserRepository
import com.example.rl_v1.repository.UserRepository
import com.example.rl_v1.storage.FirebaseImageUploader
import com.example.rl_v1.storage.ImageUploader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class UserViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val userRepo: UserRepository = FirestoreUserRepository()
    private val deviceIdProvider = SharedPrefsDeviceIdProvider(application)
    private val imageUploader: ImageUploader = FirebaseImageUploader()

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    private val _allUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val allUsers: StateFlow<List<UserProfile>> = _allUsers.asStateFlow()

    private val _isLoadingUsers = MutableStateFlow(false)
    val isLoadingUsers: StateFlow<Boolean> = _isLoadingUsers.asStateFlow()

    private val _usersErrorMessage = MutableStateFlow<String?>(null)
    val usersErrorMessage: StateFlow<String?> = _usersErrorMessage.asStateFlow()

    init {
        startObservingUser()
    }

    fun startObservingUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                ensureFirebaseSession()
                val userId = deviceIdProvider.getDeviceId()
                userRepo.observeUser(userId)
                    .catch { throwable ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = throwable.localizedMessage ?: "Failed to observe profile."
                        )
                    }
                    .collect { profile ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            profile = profile,
                            errorMessage = null
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Failed to initialize session."
                )
            }
        }
    }

    fun updateContactInfo(
        name: String,
        email: String,
        phone: String,
        onResult: (Result<Unit>) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                ensureFirebaseSession()
                val userId = deviceIdProvider.getDeviceId()
                val result = userRepo.updateContactInfo(userId, name, email, phone)
                onResult(result)
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    fun updateProfileImage(
        photoUrl: String?,
        onResult: (Result<Unit>) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                ensureFirebaseSession()
                val userId = deviceIdProvider.getDeviceId()
                val result = userRepo.updateProfileImage(userId, photoUrl)
                onResult(result)
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    /**
     * Saves the profile picture chosen on the profile photo screen.
     *
     * - `null`/blank: deletes the stored picture (best effort) and clears `photoUrl`.
     * - A remote `http(s)` URL (e.g. the existing download URL): saved as is.
     * - A local `content://`/`file://` URI: uploaded to Firebase Storage first, then the returned
     *   download URL is saved as `photoUrl`.
     *
     * @param photoUri The picked image URI or existing URL as a string, or `null` to remove it.
     * @param onResult Called with the saved `photoUrl` (or `null` when removed), or the failure.
     */
    fun saveProfilePicture(
        photoUri: String?,
        onResult: (Result<String?>) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                ensureFirebaseSession()
                val userId = deviceIdProvider.getDeviceId()
                val newPhotoUrl: String? = when {
                    photoUri.isNullOrBlank() -> {
                        runCatching { imageUploader.deleteProfilePicture(userId) }
                        null
                    }
                    photoUri.startsWith("http://") || photoUri.startsWith("https://") -> photoUri
                    else -> imageUploader.uploadProfilePicture(userId, Uri.parse(photoUri))
                }
                val result = userRepo.updateProfileImage(userId, newPhotoUrl)
                onResult(result.map { newPhotoUrl })
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    /**
     * Uploads an already cropped/compressed JPEG profile picture to Firebase Storage and saves the
     * returned download URL as the user's `photoUrl`.
     *
     * @param jpegBytes The encoded picture to upload.
     * @param onResult Called with the saved download URL, or the failure.
     */
    fun saveProfilePicture(
        jpegBytes: ByteArray,
        onResult: (Result<String>) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                ensureFirebaseSession()
                val userId = deviceIdProvider.getDeviceId()
                val downloadUrl = imageUploader.uploadProfilePicture(userId, jpegBytes)
                val result = userRepo.updateProfileImage(userId, downloadUrl)
                onResult(result.map { downloadUrl })
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    fun setNotificationsEnabled(
        enabled: Boolean,
        onResult: (Result<Unit>) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                ensureFirebaseSession()
                val userId = deviceIdProvider.getDeviceId()
                val result = userRepo.setNotificationsEnabled(userId, enabled)
                onResult(result)
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    fun updateUserRole(
        role: UserRole,
        granted: Boolean,
        onResult: (Result<Unit>) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                ensureFirebaseSession()
                val userId = deviceIdProvider.getDeviceId()
                val result = userRepo.updateUserRole(userId, role, granted)
                onResult(result)
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    /**
     * Marks the current user's profile as deleted ([UserStatus.DELETED]) when [deleted] is `true`,
     * or restores a self-deleted profile to [UserStatus.ACTIVE] when [deleted] is `false`.
     */
    fun setProfileDeleted(
        deleted: Boolean,
        onResult: (Result<Unit>) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                ensureFirebaseSession()
                val userId = deviceIdProvider.getDeviceId()
                val result = userRepo.setProfileDeleted(userId, deleted)
                onResult(result)
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    /** Kept for existing callers; delegates to [setProfileDeleted]. */
    fun deleteProfile(onResult: (Result<Unit>) -> Unit = {}) {
        setProfileDeleted(deleted = true, onResult = onResult)
    }

    /** Kept for existing callers; delegates to [setProfileDeleted]. */
    fun reactivateAccount(onResult: (Result<Unit>) -> Unit = {}) {
        setProfileDeleted(deleted = false, onResult = onResult)
    }

    /** Loads all user profiles for admin browsing (`US 03.05.01`) and filtering. */
    fun loadAllUsers() {
        viewModelScope.launch {
            _isLoadingUsers.value = true
            _usersErrorMessage.value = null
            try {
                ensureFirebaseSession()
                userRepo.getAllUsers()
                    .onSuccess { profiles ->
                        _allUsers.value = profiles
                    }
                    .onFailure { throwable ->
                        _usersErrorMessage.value =
                            throwable.localizedMessage ?: "Failed to load user profiles."
                    }
            } catch (e: Exception) {
                _usersErrorMessage.value =
                    e.localizedMessage ?: "Failed to initialize session."
            } finally {
                _isLoadingUsers.value = false
            }
        }
    }

    /**
     * Sets or revokes admin removal ([UserStatus.REMOVED] vs. [UserStatus.ACTIVE]) for [targetUserId]
     * then refreshes [allUsers].
     */
    fun setProfileRemovedByAdmin(
        targetUserId: String,
        removed: Boolean,
        onResult: (Result<Unit>) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                ensureFirebaseSession()
                val currentUserId = deviceIdProvider.getDeviceId()
                if (removed && targetUserId == currentUserId) {
                    val failure = Result.failure<Unit>(
                        IllegalStateException("You cannot remove your own active admin profile.")
                    )
                    onResult(failure)
                    return@launch
                }

                val result = userRepo.setProfileRemovedByAdmin(targetUserId, removed)
                if (result.isSuccess) {
                    loadAllUsers()
                }
                onResult(result)
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }
}