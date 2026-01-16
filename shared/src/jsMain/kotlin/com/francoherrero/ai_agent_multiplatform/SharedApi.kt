@file:OptIn(ExperimentalJsExport::class)

package com.francoherrero.ai_agent_multiplatform

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
class SharedApi {
    fun ping(): String = "pong"
}
