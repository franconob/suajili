package com.francoherrero.ai_agent_multiplatform.db

import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone


object Conversations : UUIDTable("conversations") {
    val title = text("title").nullable()
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")
}

object Messages : UUIDTable("messages") {
    val conversationId = uuid("conversation_id").references(Conversations.id)
    val role = varchar("role", 16) // USER / ASSISTANT / SYSTEM
    val content = text("content")
    val clientMessageId = varchar("client_message_id", 128).nullable()
    val status = varchar("status", 16) // sent / streaming / done / failed
    val createdAt = timestampWithTimeZone("created_at")
}
