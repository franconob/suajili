package com.francoherrero.ai_agent_multiplatform.db

import com.francoherrero.ai_agent_multiplatform.db.Conversations.createdAt
import com.francoherrero.ai_agent_multiplatform.model.ConversationDto
import com.francoherrero.ai_agent_multiplatform.model.MessageDto
import org.jetbrains.exposed.v1.core.SortOrder.*
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID

class ChatRepository {

    fun listConversations(): List<ConversationDto> = transaction {
        Conversations
            .selectAll()
            .orderBy(createdAt, DESC)
            .map { it.toConversationDto() }
    }

    fun createConversation(title: String?): ConversationDto = transaction {
        val id = Conversations.insert {
            it[Conversations.title] = title
            // created_at/updated_at default in DB
        } get Conversations.id

        Conversations
            .selectAll()
            .where(Conversations.id.eq(id))
            .single()
            .toConversationDto()
    }

    fun listMessages(conversationId: UUID, limit: Int = 200): List<MessageDto> = transaction {
        Messages
            .selectAll()
            .where { Messages.conversationId eq conversationId }
            .orderBy(Messages.createdAt, ASC)
            .limit(limit)
            .map { it.toMessageDto() }
    }

    fun insertUserMessage(
        conversationId: UUID,
        content: String,
        clientMessageId: String?
    ): MessageDto = transaction {
        val id = Messages.insert {
            it[Messages.conversationId] = conversationId
            it[Messages.role] = "USER"
            it[Messages.content] = content
            it[Messages.clientMessageId] = clientMessageId
            it[Messages.status] = "sent"
        } get Messages.id

        Messages.selectAll().where(Messages.id.eq(id)).single().toMessageDto()
    }

    fun insertAssistantFinalMessage(
        conversationId: UUID,
        content: String
    ): MessageDto = transaction {
        val id = Messages.insert {
            it[Messages.conversationId] = conversationId
            it[Messages.role] = "ASSISTANT"
            it[Messages.content] = content
            it[Messages.clientMessageId] = null
            it[Messages.status] = "done"
        } get Messages.id

        Messages.selectAll().where(Messages.id.eq(id)).single().toMessageDto()
    }

    fun conversationExists(conversationId: UUID): Boolean = transaction {
        Conversations.selectAll().where(Conversations.id.eq(conversationId)).any()
    }
}
