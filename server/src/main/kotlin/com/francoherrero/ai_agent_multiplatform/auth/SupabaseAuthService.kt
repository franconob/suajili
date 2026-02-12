package com.francoherrero.ai_agent_multiplatform.auth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
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

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String, val code: Int? = null) : AuthResult<Nothing>()
}

class SupabaseAuthService(
    private val supabaseUrl: String,
    private val supabaseAnonKey: String
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
            header("apikey", supabaseAnonKey)
        }
    }

    suspend fun login(email: String, password: String): AuthResult<AuthResponse> {
        return try {
            val response = client.post("$supabaseUrl/auth/v1/token?grant_type=password") {
                setBody(LoginRequest(email, password))
            }

            if (response.status == HttpStatusCode.OK) {
                val body = response.bodyAsText()
                println("BODY: $body")
                val authResponse = response.body<AuthResponse>()
                AuthResult.Success(authResponse)
            } else {
                val error = parseError(response.bodyAsText())
                AuthResult.Error(
                    message = error?.errorDescription ?: error?.message ?: "Login failed",
                    code = response.status.value
                )
            }
        } catch (e: Exception) {
            AuthResult.Error(message = e.message ?: "Unknown error")
        }
    }

    suspend fun register(email: String, password: String, fullName: String? = null): AuthResult<AuthResponse> {
        return try {
            val response = client.post("$supabaseUrl/auth/v1/signup") {
                setBody(RegisterRequest(email, password, fullName?.let { com.francoherrero.ai_agent_multiplatform.auth.RegisterData(it) }))
            }

            if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created) {
                val authResponse = response.body<AuthResponse>()
                AuthResult.Success(authResponse)
            } else {
                val error = parseError(response.bodyAsText())
                AuthResult.Error(
                    message = error?.errorDescription ?: error?.message ?: "Registration failed",
                    code = response.status.value
                )
            }
        } catch (e: Exception) {
            AuthResult.Error(message = e.message ?: "Unknown error")
        }
    }

    suspend fun refreshToken(refreshToken: String): AuthResult<AuthResponse> {
        return try {
            val response = client.post("$supabaseUrl/auth/v1/token?grant_type=refresh_token") {
                setBody(RefreshTokenRequest(refreshToken))
            }

            if (response.status == HttpStatusCode.OK) {
                val authResponse = response.body<AuthResponse>()
                AuthResult.Success(authResponse)
            } else {
                val error = parseError(response.bodyAsText())
                AuthResult.Error(
                    message = error?.errorDescription ?: error?.message ?: "Token refresh failed",
                    code = response.status.value
                )
            }
        } catch (e: Exception) {
            AuthResult.Error(message = e.message ?: "Unknown error")
        }
    }

    suspend fun logout(accessToken: String): AuthResult<Unit> {
        return try {
            val response = client.post("$supabaseUrl/auth/v1/logout") {
                header("Authorization", "Bearer $accessToken")
            }

            if (response.status == HttpStatusCode.NoContent || response.status == HttpStatusCode.OK) {
                AuthResult.Success(Unit)
            } else {
                AuthResult.Error(
                    message = "Logout failed",
                    code = response.status.value
                )
            }
        } catch (e: Exception) {
            AuthResult.Error(message = e.message ?: "Unknown error")
        }
    }

    private fun parseError(body: String): AuthError? {
        return try {
            json.decodeFromString<AuthError>(body)
        } catch (e: Exception) {
            null
        }
    }
}
