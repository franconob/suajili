package com.francoherrero.ai_agent_multiplatform.chat

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class ChatDelta(val conversationId: String, val text: String)
data class ChatDone(val conversationId: String)

sealed interface ChatStreamEvent {
    val conversationId: String

    data class Delta(override val conversationId: String, val text: String) : ChatStreamEvent
    data class Done(override val conversationId: String) : ChatStreamEvent
}

object ChatBus {
    private val mutex = Mutex()
    private val streams = mutableMapOf<String, MutableSharedFlow<ChatStreamEvent>>()

    suspend fun stream(conversationId: String): SharedFlow<ChatStreamEvent> =
        mutex.withLock {
            streams.getOrPut(conversationId) {
                MutableSharedFlow(
                    replay = 0,
                    extraBufferCapacity = 64
                )
            }
        }.asSharedFlow()

    suspend fun emit(event: ChatStreamEvent) {
        val mutable = mutex.withLock { streams[event.conversationId]  } ?: return

        mutable.tryEmit(event)
    }
}