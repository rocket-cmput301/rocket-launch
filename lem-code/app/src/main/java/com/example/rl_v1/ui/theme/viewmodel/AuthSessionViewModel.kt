package com.example.rl_v1.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rl_v1.model.DeviceIdProvider
import com.example.rl_v1.model.SessionUiState
import com.example.rl_v1.model.UserRole
import com.example.rl_v1.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Manages device-based sign-in initialization, active profile observation,
 * and active role selection for multi-role users (especially admins).
 * @param deviceIdProvider Provides user id used for session identification.
 * @param userRepository Provides user profiles.
 */
class AuthSessionViewModel(
    private val deviceIdProvider: DeviceIdProvider,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    init {
        startSession()
    }

    /**
     * Initializes or restarts the session using the device identifier as `userId`.
     */
    fun startSession() {
        val userId = deviceIdProvider.getDeviceId()
        _uiState.update {
            it.copy(isLoading = true, userId = userId, errorMessage = null)
        }

        viewModelScope.launch {
            userRepository.observeUser(userId)
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Failed to load user session."
                        )
                    }
                }
                .collect { profile ->
                    val availableRoles = profile?.role?.ifEmpty { listOf(UserRole.ENTRANT) }
                    _uiState.update { current ->
                        val nextRole = if (availableRoles?.contains(current.activeRole) == true ) {
                            current.activeRole
                        } else {
                            availableRoles?.first()
                        }
                        current.copy(
                            isLoading = false,
                            userId = profile?.userId?.ifBlank { userId } ?: "",
                            profile = profile,
                            activeRole = nextRole,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    /**
     * Switches the active UI role if the user holds [role].
     */
    fun switchRole(role: UserRole) {
        val currentProfile = _uiState.value.profile ?: return
        if (role in currentProfile.role) {
            _uiState.update { it.copy(activeRole = role) }
        }
    }
}