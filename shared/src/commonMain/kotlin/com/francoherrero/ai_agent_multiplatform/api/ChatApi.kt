package com.francoherrero.ai_agent_multiplatform.api

import com.francoherrero.ai_agent_multiplatform.model.MessageListResponse
import kotlinx.serialization.json.Json


sealed interface ApiResult<out T> {
    data class Ok<T>(val data: T) : ApiResult<T>
    data class Error(val message: String, val code: Int?): ApiResult<Nothing>
}

object ChatApi {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun fetchMessageList(): ApiResult<MessageListResponse> {
        return ApiResult.Ok(MessageListResponse(emptyList()))
//        return try {
//            val rawJson = HttpClient.get("/messages")
//            val response = json.decodeFromString<MessageListResponse>(rawJson)
//            ApiResult.Ok(response)
//        } catch (e: Exception) {
//            ApiResult.Error(
//                message = e.message ?: "Unknown error",
//                code = null
//            )
//        }
    }

    fun listenSse() {
        //HttpClient.client.sse()
    }
}