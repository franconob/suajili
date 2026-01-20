package com.francoherrero.ai_agent_multiplatform.chat

import ai.koog.prompt.streaming.StreamFrame
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed interface ChatStreamEvent {
    val conversationId: String

    data class Frame(override val conversationId: String, val frame: StreamFrame) : ChatStreamEvent
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

fun chunkForStreaming(text: String, max: Int = 180): List<String> {
    val chunks = mutableListOf<String>()
    var remaining = text

    while (remaining.length > max) {
        val window = remaining.take(max)

        val splitIndex =
            window.lastIndexOf('\n').takeIf { it > 0 }
                ?: window.lastIndexOf(". ").takeIf { it > 0 }
                ?: window.lastIndexOf(", ").takeIf { it > 0 }
                ?: window.lastIndexOf(' ').takeIf { it > 0 }
                ?: max

        chunks += remaining.take(splitIndex + 1)
        remaining = remaining.drop(splitIndex + 1)
    }

    if (remaining.isNotBlank()) chunks += remaining
    return chunks
}
