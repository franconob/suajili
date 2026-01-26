package com.francoherrero.ai_agent_multiplatform.chat

import com.francoherrero.ai_agent_multiplatform.ai.SUAJILI_SYSTEM_PROMPT
import com.francoherrero.ai_agent_multiplatform.model.MessageDto

fun buildAgentInput(conversation: List<MessageDto>): String {
    val sb = StringBuilder()

    sb.appendLine("### SISTEMA ###")
    sb.appendLine(SUAJILI_SYSTEM_PROMPT.trim())
    sb.appendLine()
    sb.appendLine("### CHAT ###")
    conversation.forEach { m ->
        when (m.role) {
            "SYSTEM" -> sb.appendLine("SISTEMA: ${m.content}")
            "USER" -> sb.appendLine("USUARIO: ${m.content}")
            "ASSISTANT" -> sb.appendLine("ASISTENTE: ${m.content}")
        }
    }
    sb.appendLine("### FIN ###")

    return sb.toString().trim()
}