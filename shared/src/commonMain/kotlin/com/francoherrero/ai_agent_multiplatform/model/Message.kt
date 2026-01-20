@file:OptIn(ExperimentalJsExport::class)

package com.francoherrero.ai_agent_multiplatform.model

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
enum class Role {
    USER,
    SYSTEM,
    ASSISTANT
}

@Serializable
data class Message @OptIn(ExperimentalUuidApi::class) constructor(
    val id: String = Uuid.random().toString(),
    val timestamp: String = Clock.System.now().toString(),
    val content: String,
    val role: Role
)