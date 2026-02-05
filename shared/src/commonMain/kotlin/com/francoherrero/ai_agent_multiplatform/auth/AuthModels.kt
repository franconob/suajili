package com.francoherrero.ai_agent_multiplatform.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    @SerialName("user_metadata")
    val userMetadata: UserMetadata? = null
)

@Serializable
data class UserMetadata(
    @SerialName("full_name")
    val fullName: String? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val data: RegisterData? = null
)

@Serializable
data class RegisterData(
    @SerialName("full_name")
    val fullName: String? = null
)

@Serializable
data class AuthResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("token_type")
    val tokenType: String = "bearer",
    @SerialName("expires_in")
    val expiresIn: Long,
    @SerialName("expires_at")
    val expiresAt: Long? = null,
    val user: User
)

@Serializable
data class RefreshTokenRequest(
    @SerialName("refresh_token")
    val refreshToken: String
)

@Serializable
data class AuthError(
    val error: String? = null,
    @SerialName("error_description")
    val errorDescription: String? = null,
    val message: String? = null
)

sealed class AuthState {
    data object Loading : AuthState()
    data object NotAuthenticated : AuthState()
    data class Authenticated(val user: User, val accessToken: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
