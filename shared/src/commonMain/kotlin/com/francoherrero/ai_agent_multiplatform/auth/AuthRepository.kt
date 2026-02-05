package com.francoherrero.ai_agent_multiplatform.auth

import com.francoherrero.ai_agent_multiplatform.api.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    suspend fun checkAuthState() {
        val accessToken = tokenStorage.getAccessToken()
        val userId = tokenStorage.getUserId()
        val email = tokenStorage.getUserEmail()

        if (accessToken != null && userId != null && email != null) {
            val user = User(
                id = userId,
                email = email,
                userMetadata = tokenStorage.getUserFullName()?.let { UserMetadata(fullName = it) }
            )
            _authState.value = AuthState.Authenticated(user, accessToken)
        } else {
            _authState.value = AuthState.NotAuthenticated
        }
    }

    suspend fun login(email: String, password: String): ApiResult<AuthResponse> {
        _authState.value = AuthState.Loading

        val result = authApi.login(email, password)

        when (result) {
            is ApiResult.Ok -> {
                val response = result.data
                tokenStorage.saveTokens(response.accessToken, response.refreshToken)
                tokenStorage.saveUser(
                    userId = response.user.id,
                    email = response.user.email,
                    fullName = response.user.userMetadata?.fullName
                )
                _authState.value = AuthState.Authenticated(response.user, response.accessToken)
            }
            is ApiResult.Error -> {
                _authState.value = AuthState.Error(result.message)
            }
        }

        return result
    }

    suspend fun register(email: String, password: String, fullName: String? = null): ApiResult<AuthResponse> {
        _authState.value = AuthState.Loading

        val result = authApi.register(email, password, fullName)

        when (result) {
            is ApiResult.Ok -> {
                val response = result.data
                tokenStorage.saveTokens(response.accessToken, response.refreshToken)
                tokenStorage.saveUser(
                    userId = response.user.id,
                    email = response.user.email,
                    fullName = response.user.userMetadata?.fullName ?: fullName
                )
                _authState.value = AuthState.Authenticated(response.user, response.accessToken)
            }
            is ApiResult.Error -> {
                _authState.value = AuthState.Error(result.message)
            }
        }

        return result
    }

    suspend fun refreshToken(): ApiResult<AuthResponse>? {
        val refreshToken = tokenStorage.getRefreshToken() ?: return null

        val result = authApi.refreshToken(refreshToken)

        when (result) {
            is ApiResult.Ok -> {
                val response = result.data
                tokenStorage.saveTokens(response.accessToken, response.refreshToken)
                _authState.value = AuthState.Authenticated(response.user, response.accessToken)
            }
            is ApiResult.Error -> {
                logout()
            }
        }

        return result
    }

    suspend fun logout() {
        val accessToken = tokenStorage.getAccessToken()
        if (accessToken != null) {
            authApi.logout(accessToken)
        }

        tokenStorage.clearTokens()
        _authState.value = AuthState.NotAuthenticated
    }

    suspend fun getAccessToken(): String? = tokenStorage.getAccessToken()

    suspend fun isLoggedIn(): Boolean = tokenStorage.getAccessToken() != null
}
