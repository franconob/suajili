@file:OptIn(ExperimentalJsExport::class)

package com.francoherrero.ai_agent_multiplatform.model

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport

@Serializable
enum class Role {
    USER,
    SYSTEM
}

@Serializable
data class Message(
    val id: String,
    val timestamp: String,
    val content: String,
    val role: Role
)
