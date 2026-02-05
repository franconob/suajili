package com.francoherrero.ai_agent_multiplatform.model

import kotlinx.serialization.Serializable

@Serializable
data class ConversationDto(
    val id: String,
    val title: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class MessageDto(
    val id: String,
    val conversationId: String,
    val role: String,
    val content: String,
    val clientMessageId: String? = null,
    val status: String,
    val createdAt: String
)

@Serializable
data class CreateConversationRequest(val title: String? = null)

@Serializable
data class CreateMessageRequest(
    val content: String,
    val clientMessageId: String? = null
)

@Serializable
data class CreateConversationResponse(val conversation: ConversationDto)

@Serializable
data class CreateMessageResponse(val message: MessageDto)