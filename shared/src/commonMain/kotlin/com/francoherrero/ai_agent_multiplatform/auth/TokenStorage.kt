package com.francoherrero.ai_agent_multiplatform.auth

interface TokenStorage {
    suspend fun saveTokens(accessToken: String, refreshToken: String)
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun clearTokens()
    suspend fun saveUser(userId: String, email: String, fullName: String?)
    suspend fun getUserId(): String?
    suspend fun getUserEmail(): String?
    suspend fun getUserFullName(): String?

    companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_EMAIL = "user_email"
        const val KEY_USER_FULL_NAME = "user_full_name"
    }
}
