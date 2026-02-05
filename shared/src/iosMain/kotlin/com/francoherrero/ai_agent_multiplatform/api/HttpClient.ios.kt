package com.francoherrero.ai_agent_multiplatform.api

import com.francoherrero.ai_agent_multiplatform.auth.TokenStorage
import sp.bvantur.inspektify.ktor.InspektifyKtor
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

actual fun createHttpClient(baseUrl: String, tokenStorage: TokenStorage): HttpClient {
    return HttpClient(Darwin) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(InspektifyKtor)
        defaultRequest {
            url(baseUrl)
            contentType(ContentType.Application.Json)
            runBlocking {
                tokenStorage.getAccessToken()?.let {
                    header("Authorization", "Bearer $it")
                }
            }
        }
    }
}
