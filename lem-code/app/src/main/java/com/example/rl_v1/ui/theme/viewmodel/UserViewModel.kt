package com.example.rl_v1.ui.theme.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rl_v1.model.SharedPrefsDeviceIdProvider
import com.example.rl_v1.model.UserRole
import com.example.rl_v1.model.UserUiState
import com.example.rl_v1.repository.FirestoreUserRepository
import com.example.rl_v1.repository.UserRepository
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

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

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
                        _uiState.value = UserUiState(
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

    fun deleteProfile(onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            try {
                ensureFirebaseSession()
                val userId = deviceIdProvider.getDeviceId()
                val result = userRepo.deleteProfile(userId)
                onResult(result)
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    fun reactivateAccount(onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            try {
                ensureFirebaseSession()
                val userId = deviceIdProvider.getDeviceId()
                val result = userRepo.reactivateUser(userId)
                onResult(result)
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }
}