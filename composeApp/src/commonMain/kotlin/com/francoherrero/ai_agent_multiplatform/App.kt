package com.francoherrero.ai_agent_multiplatform

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedTextField
import com.francoherrero.ai_agent_multiplatform.api.ApiResult
import com.francoherrero.ai_agent_multiplatform.api.ChatApi
import com.francoherrero.ai_agent_multiplatform.api.HttpClient
import com.francoherrero.ai_agent_multiplatform.model.Message
import kotlinx.coroutines.launch

@Composable
fun App() {
    MaterialTheme {
        val inputState = rememberTextFieldState("")
        val coroutineScope = rememberCoroutineScope()
        var messageList by remember { mutableStateOf(emptyList<Message>()) }

        val handleOnClick = {

        }

        LaunchedEffect(true) {
            when (val response = ChatApi.fetchMessageList()) {
                is ApiResult.Ok -> {
                    println("Es OK!")
                    messageList = response.data.messages
                }

                is ApiResult.Error -> {
                    println("Error: ${response.message}")
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            for (message in messageList) {
                Row(modifier = Modifier.fillMaxHeight(2 / 3f)) {
                    Text(message.content)
                }
            }

            Row(modifier = Modifier.fillMaxHeight()) {
                Column {
                    OutlinedTextField(
                        state = inputState
                    )
                }
                Column {
                    ElevatedButton(onClick = { handleOnClick }) {
                        Text("Send!")
                    }
                }
            }
        }
    }
}