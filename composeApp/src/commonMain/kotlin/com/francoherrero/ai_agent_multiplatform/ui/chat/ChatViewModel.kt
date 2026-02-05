package com.francoherrero.ai_agent_multiplatform.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francoherrero.ai_agent_multiplatform.api.ApiResult
import com.francoherrero.ai_agent_multiplatform.api.ChatApi
import com.francoherrero.ai_agent_multiplatform.model.Message
import com.francoherrero.ai_agent_multiplatform.model.Role
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface ChatUiState {
    data object Idle : ChatUiState
    data object Loading : ChatUiState
    data object SendingMessage : ChatUiState
    data class Error(val message: String) : ChatUiState
}

class ChatViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Idle)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    fun loadMessages() {
        viewModelScope.launch {
            _uiState.value = ChatUiState.Loading

            when (val result = ChatApi.fetchMessageList()) {
                is ApiResult.Ok -> {
                    _messages.value = result.data.messages
                    _uiState.value = ChatUiState.Idle
                }
                is ApiResult.Error -> {
                    _uiState.value = ChatUiState.Error(result.message)
                }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun sendMessage(content: String) {
        viewModelScope.launch {
            // Add optimistic user message
            val userMessage = Message(
                id = Uuid.random().toString(),
                timestamp = "",
                content = content,
                role = Role.USER
            )
            _messages.value = _messages.value + userMessage
            _uiState.value = ChatUiState.SendingMessage

            // TODO: Implement actual message sending via API
            // For now, simulate a response
            delay(1500)

            val assistantMessage = Message(
                id = Uuid.random().toString(),
                timestamp = "",
                content = "Gracias por tu mensaje. Esta funcion estara disponible pronto. Por favor, contacta con nosotros directamente para consultas urgentes.",
                role = Role.ASSISTANT
            )
            _messages.value = _messages.value + assistantMessage
            _uiState.value = ChatUiState.Idle
        }
    }
}
