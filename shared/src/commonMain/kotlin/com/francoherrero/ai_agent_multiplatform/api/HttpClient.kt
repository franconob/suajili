package com.francoherrero.ai_agent_multiplatform.api

import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post

expect fun createHttpClient(baseUrl: String): io.ktor.client.HttpClient

object HttpClient {
    val client = createHttpClient("http://localhost:8080/")

    suspend fun get(url: String): String =
        client.get(url).let { response ->
            return  response.body()
        }


    suspend fun post(builder: HttpRequestBuilder) = client.post(builder)

}