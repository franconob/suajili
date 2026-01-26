package com.francoherrero.ai_agent_multiplatform.routes

import ai.koog.agents.core.agent.ToolCalls
import ai.koog.ktor.aiAgent
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.streaming.StreamFrame
import com.francoherrero.ai_agent_multiplatform.model.CreateConversationRequest
import com.francoherrero.ai_agent_multiplatform.model.CreateConversationResponse
import com.francoherrero.ai_agent_multiplatform.model.CreateMessageRequest
import com.francoherrero.ai_agent_multiplatform.model.CreateMessageResponse
import com.francoherrero.ai_agent_multiplatform.model.MessageDto
import com.francoherrero.ai_agent_multiplatform.asTextOrNull
import com.francoherrero.ai_agent_multiplatform.chat.ChatBus
import com.francoherrero.ai_agent_multiplatform.chat.ChatStreamEvent
import com.francoherrero.ai_agent_multiplatform.chat.buildAgentInput
import com.francoherrero.ai_agent_multiplatform.chat.chunkForStreaming
import com.francoherrero.ai_agent_multiplatform.db.ChatRepository
import com.francoherrero.ai_agent_multiplatform.model.ConversationDto
import com.francoherrero.ai_agent_multiplatform.model.DeltaPayload
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.sse
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.UUID
import kotlin.collections.emptyList

fun Route.chatRoutes(repo: ChatRepository) {

    val json = Json {
        prettyPrint = true
        encodeDefaults = true
        explicitNulls = false
    }

    route("/conversations") {

        get({
            description = "Get a list of conversations"
            response {
                code(HttpStatusCode.OK) {
                    body<List<ConversationDto>>()
                }
            }
        }) {
            val convos = repo.listConversations()
            call.respond(convos)
        }

        post({
            description = "Create a new conversation"
            request {
                body<CreateConversationRequest> {
                    required = true
                }
            }
            response {
                HttpStatusCode.Created to {
                    description = "Created"
                    body<CreateConversationResponse>()
                }
            }
        }) {
            val req = call.receive<CreateConversationRequest>()
            val convo = repo.createConversation(req.title)
            call.respond(HttpStatusCode.Created, CreateConversationResponse(convo))
        }

        route("/{conversationId}") {

            get("/messages", {
                description = "Get a list of conversation messages"
                response {
                   code(HttpStatusCode.OK) {
                       body<List<MessageDto>>()
                   }
                }
            }) {
                val conversationIdStr = call.parameters["conversationId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing conversationId")

                val conversationId = runCatching { UUID.fromString(conversationIdStr) }.getOrNull()
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        "conversationId must be UUID"
                    )

                if (!repo.conversationExists(conversationId)) {
                    // return empty history for unknown conversations (keeps FE simple)
                    return@get call.respond(message = emptyList<MessageDto>())
                }

                val messages = repo.listMessages(conversationId)
                call.respond(messages)
            }

            post("/messages", {
                description = "Append a user message"
                request {
                    body<CreateMessageRequest> {
                        required = true
                    }
                }
                response {
                    HttpStatusCode.Created to {
                        body<MessageDto>()
                    }
                }
            }) {
                val newMessage = call.receive<CreateMessageRequest>()
                val conversationIdStr = call.parameters["conversationId"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing conversationId")

                val conversationId = runCatching { UUID.fromString(conversationIdStr) }.getOrNull()
                    ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        "conversationId must be UUID"
                    )

                // If you want server to accept "new" ids without a separate create endpoint:
                //repo.createConversationIfMissing(conversationId)

                // 1) Persist USER message immediately so FE can replace placeholder with real id/timestamp
                val storedUser = repo.insertUserMessage(
                    conversationId = conversationId,
                    content = newMessage.content,
                    clientMessageId = newMessage.clientMessageId // later: accept from FE
                )

                // Respond right away (important: don’t block on AI)
                call.respond(HttpStatusCode.Created, storedUser)

                // 2) Run agent + stream deltas; persist final assistant at end
                call.application.launch {
                    val assistantBuffer = StringBuilder()

                    try {
                        val conversation = repo.listMessages(conversationId)
                        val input = buildAgentInput(conversation)

                        val answer = this@post.aiAgent(
                            input = input,
                            model = OpenAIModels.Chat.GPT4_1,
                            runMode = ToolCalls.SINGLE_RUN_SEQUENTIAL
                        )

                        chunkForStreaming(answer).forEach { chunk ->
                            assistantBuffer.append(chunk)
                            ChatBus.emit(
                                ChatStreamEvent.Frame(
                                    conversationId.toString(),
                                    StreamFrame.Append(chunk)
                                )
                            )
                            delay(25)
                        }

                        repo.insertAssistantFinalMessage(conversationId, assistantBuffer.toString())

                        ChatBus.emit(
                            ChatStreamEvent.Frame(
                                conversationId.toString(),
                                StreamFrame.End()
                            )
                        )
                    } catch (t: Throwable) {
                        t.printStackTrace()
                        ChatBus.emit(
                            ChatStreamEvent.Frame(
                                conversationId.toString(),
                                StreamFrame.Append("\n\n❌ Error: ${t.message ?: "unknown"}")
                            )
                        )
                        ChatBus.emit(
                            ChatStreamEvent.Frame(
                                conversationId.toString(),
                                StreamFrame.End()
                            )
                        )
                    }
                }
            }

            sse("/stream") {
                val conversationId =
                    call.parameters["conversationId"] ?: return@sse run {
                        send(ServerSentEvent(event = "error", data = "missing conversationId"))
                    }

                send(ServerSentEvent(event = "ready", data = "ok"))

                val pingJob = launch {
                    while (isActive) {
                        delay(15_000)
                        send(ServerSentEvent(event = "ping", data = "1"))
                    }
                }

                try {
                    val stream = ChatBus.stream(conversationId)
                    stream.collect { ev ->
                        when (ev) {
                            is ChatStreamEvent.Frame -> {
                                val frame = ev.frame

                                frame.asTextOrNull()?.let { text ->
                                    val payload = json.encodeToString(DeltaPayload(text))
                                    send(ServerSentEvent(event = "delta", data = payload))
                                }

                                if (frame is StreamFrame.End) {
                                    send(ServerSentEvent(event = "done", data = "done"))
                                }

                                if (frame is StreamFrame.ToolCall) {
                                    send(
                                        ServerSentEvent(
                                            event = "tool",
                                            data = "${frame.name}:${frame.content}"
                                        )
                                    )
                                }
                            }
                        }
                    }
                } finally {
                    pingJob.cancel()
                }
            }
        }
    }
}
