package com.francoherrero.ai_agent_multiplatform.db

import com.francoherrero.ai_agent_multiplatform.model.ConversationDto
import com.francoherrero.ai_agent_multiplatform.model.MessageDto
import org.jetbrains.exposed.v1.core.ResultRow
import java.time.format.DateTimeFormatter

private val ISO = DateTimeFormatter.ISO_OFFSET_DATE_TIME

fun ResultRow.toConversationDto(): ConversationDto = ConversationDto(
    id = this[Conversations.id].value.toString(),
    title = this[Conversations.title],
    createdAt = ISO.format(this[Conversations.createdAt]),
    updatedAt = ISO.format(this[Conversations.updatedAt])
)

fun ResultRow.toMessageDto(): MessageDto = MessageDto(
    id = this[Messages.id].value.toString(),
    conversationId = this[Messages.conversationId].toString(),
    role = this[Messages.role],
    content = this[Messages.content],
    clientMessageId = this[Messages.clientMessageId],
    status = this[Messages.status],
    createdAt = ISO.format(this[Messages.createdAt])
)
