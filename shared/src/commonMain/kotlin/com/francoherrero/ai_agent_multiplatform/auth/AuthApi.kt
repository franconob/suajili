package com.francoherrero.ai_agent_multiplatform.auth

import com.francoherrero.ai_agent_multiplatform.api.ApiResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class AuthApi(
    private val client: HttpClient
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun login(email: String, password: String): ApiResult<AuthResponse> {
        return try {
            val response = client.post("/auth/login") {
                setBody(LoginRequest(email, password))
            }

            if (response.status == HttpStatusCode.OK) {
                val authResponse = response.body<AuthResponse>()
                ApiResult.Ok(authResponse)
            } else {
                val errorBody = response.bodyAsText()
                val error = try {
                    json.decodeFromString<AuthError>(errorBody)
                } catch (e: Exception) {
                    null
                }
                ApiResult.Error(
                    message = error?.errorDescription ?: error?.message ?: "Login failed",
                    code = response.status.value
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(
                message = e.message ?: "Unknown error",
                code = null
            )
        }
    }

    suspend fun register(email: String, password: String, fullName: String? = null): ApiResult<AuthResponse> {
        return try {
            val registerData = fullName?.let { RegisterData(fullName = it) }
            val response = client.post("/auth/register") {
                setBody(RegisterRequest(email, password, registerData))
            }

            if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created) {
                val authResponse = response.body<AuthResponse>()
                ApiResult.Ok(authResponse)
            } else {
                val errorBody = response.bodyAsText()
                val error = try {
                    json.decodeFromString<AuthError>(errorBody)
                } catch (e: Exception) {
                    null
                }
                ApiResult.Error(
                    message = error?.errorDescription ?: error?.message ?: "Registration failed",
                    code = response.status.value
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(
                message = e.message ?: "Unknown error",
                code = null
            )
        }
    }

    suspend fun refreshToken(refreshToken: String): ApiResult<AuthResponse> {
        return try {
            val response = client.post("/auth/refresh") {
                setBody(RefreshTokenRequest(refreshToken))
            }

            if (response.status == HttpStatusCode.OK) {
                val authResponse = response.body<AuthResponse>()
                ApiResult.Ok(authResponse)
            } else {
                val errorBody = response.bodyAsText()
                val error = try {
                    json.decodeFromString<AuthError>(errorBody)
                } catch (e: Exception) {
                    null
                }
                ApiResult.Error(
                    message = error?.errorDescription ?: error?.message ?: "Token refresh failed",
                    code = response.status.value
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(
                message = e.message ?: "Unknown error",
                code = null
            )
        }
    }

    suspend fun logout(accessToken: String): ApiResult<Unit> {
        return try {
            val response = client.post("/auth/logout") {
                header("Authorization", "Bearer $accessToken")
            }

            if (response.status == HttpStatusCode.NoContent || response.status == HttpStatusCode.OK) {
                ApiResult.Ok(Unit)
            } else {
                ApiResult.Error(
                    message = "Logout failed",
                    code = response.status.value
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(
                message = e.message ?: "Unknown error",
                code = null
            )
        }
    }
}
