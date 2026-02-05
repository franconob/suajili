package com.francoherrero.ai_agent_multiplatform.auth

import com.parkwoocheol.kmpdatastore.TypeSafeDataStore
import kotlinx.coroutines.flow.firstOrNull

class TokenStorageImpl(private val dataStore: TypeSafeDataStore): TokenStorage {
    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        dataStore.putString(TokenStorage.KEY_ACCESS_TOKEN, accessToken)
        dataStore.putString(TokenStorage.KEY_REFRESH_TOKEN, refreshToken)
    }

    override suspend fun getAccessToken(): String? =
        dataStore.getString(TokenStorage.KEY_ACCESS_TOKEN).firstOrNull()

    override suspend fun getRefreshToken(): String? =
        dataStore.getString(TokenStorage.KEY_REFRESH_TOKEN).firstOrNull()

    override suspend fun clearTokens() {
        dataStore.remove(TokenStorage.KEY_ACCESS_TOKEN)
        dataStore.remove(TokenStorage.KEY_REFRESH_TOKEN)
    }

    override suspend fun saveUser(userId: String, email: String, fullName: String?) {
        dataStore.putString(TokenStorage.KEY_USER_ID, userId)
        dataStore.putString(TokenStorage.KEY_USER_EMAIL, email)
        dataStore.putString(TokenStorage.KEY_USER_FULL_NAME, fullName.orEmpty())
    }

    override suspend fun getUserId(): String? =
        dataStore.getString(TokenStorage.KEY_USER_ID).firstOrNull()

    override suspend fun getUserEmail(): String? =
        dataStore.getString(TokenStorage.KEY_USER_EMAIL).firstOrNull()

    override suspend fun getUserFullName(): String? =
        dataStore.getString(TokenStorage.KEY_USER_FULL_NAME).firstOrNull()
}