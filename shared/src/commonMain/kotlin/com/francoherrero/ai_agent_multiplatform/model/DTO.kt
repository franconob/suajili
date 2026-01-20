package com.francoherrero.ai_agent_multiplatform.model

import kotlinx.serialization.Serializable

@Serializable
data class DeltaPayload(val delta: String)

@Serializable
data class DonePayload(val done: Boolean = true)
