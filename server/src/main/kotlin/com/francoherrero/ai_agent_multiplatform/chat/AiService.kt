package com.francoherrero.ai_agent_multiplatform.chat

import com.francoherrero.ai_agent_multiplatform.model.Message
import com.francoherrero.ai_agent_multiplatform.model.Role

import com.francoherrero.ai_agent_multiplatform.ai.SUAJILI_SYSTEM_PROMPT

fun buildAgentInput(conversation: List<Message>): String {
    val sb = StringBuilder()

    sb.appendLine("### SISTEMA ###")
    sb.appendLine(SUAJILI_SYSTEM_PROMPT.trim())
    sb.appendLine()
    sb.appendLine("### CHAT ###")
    conversation.forEach { m ->
        when (m.role) {
            Role.SYSTEM -> sb.appendLine("SISTEMA: ${m.content}")
            Role.USER -> sb.appendLine("USUARIO: ${m.content}")
            Role.ASSISTANT -> sb.appendLine("ASISTENTE: ${m.content}")
        }
    }
    sb.appendLine("### FIN ###")

    return sb.toString().trim()
}