package com.francoherrero.ai_agent_multiplatform.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francoherrero.ai_agent_multiplatform.api.ApiResult
import com.francoherrero.ai_agent_multiplatform.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

sealed interface RegisterUiState {
    data object Idle : RegisterUiState
    data object Loading : RegisterUiState
    data object Success : RegisterUiState
    data class Error(val message: String) : RegisterUiState
}

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(email: String, password: String, fullName: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading

            val result = authRepository.register(email, password, fullName)

            when (result) {
                is ApiResult.Ok -> {
                    _uiState.value = RegisterUiState.Success
                    onSuccess()
                }
                is ApiResult.Error -> {
                    _uiState.value = RegisterUiState.Error(result.message)
                }
            }
        }
    }

    fun clearError() {
        if (_uiState.value is RegisterUiState.Error) {
            _uiState.value = RegisterUiState.Idle
        }
    }
}
