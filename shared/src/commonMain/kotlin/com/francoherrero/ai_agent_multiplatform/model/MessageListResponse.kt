package com.francoherrero.ai_agent_multiplatform.model

import kotlinx.serialization.Serializable

@Serializable
data class MessageListResponse(val messages: List<Message>)
