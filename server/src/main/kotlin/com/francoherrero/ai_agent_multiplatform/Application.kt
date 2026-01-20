package com.francoherrero.ai_agent_multiplatform

import ai.koog.agents.core.agent.ToolCalls
import ai.koog.agents.core.tools.reflect.tools
import ai.koog.ktor.Koog
import ai.koog.ktor.aiAgent
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.streaming.StreamFrame
import com.francoherrero.ai_agent_multiplatform.ai.TripsTools
import com.francoherrero.ai_agent_multiplatform.chat.ChatBus
import com.francoherrero.ai_agent_multiplatform.chat.ChatStreamEvent
import com.francoherrero.ai_agent_multiplatform.chat.buildAgentInput
import com.francoherrero.ai_agent_multiplatform.chat.chunkForStreaming
import com.francoherrero.ai_agent_multiplatform.model.DeltaPayload
import com.francoherrero.ai_agent_multiplatform.model.Message
import com.francoherrero.ai_agent_multiplatform.model.MessageListResponse
import com.francoherrero.ai_agent_multiplatform.model.Role
import com.francoherrero.ai_agent_multiplatform.repository.TripsRepository
import io.github.smiley4.ktoropenapi.OpenApi
import io.github.smiley4.ktoropenapi.config.ExampleEncoder
import io.github.smiley4.ktoropenapi.config.SchemaGenerator
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.http.content.staticFiles
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receiveText
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.SSE
import io.ktor.server.sse.sse
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.collections.emptyList
import kotlin.uuid.ExperimentalUuidApi

fun StreamFrame.asTextOrNull(): String? =
    when (this) {
        is StreamFrame.Append -> this.text
        else -> null
    }

var history: MutableMap<String, MutableList<Message>> = mutableMapOf()

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

private fun Application.loadViajesJsonText(): String {
    val cfg = environment.config
    val overridePath = cfg.propertyOrNull("suajili.viajesJsonPath")?.getString()
    val classpathName = cfg.property("suajili.viajesJsonClasspath").getString()

    return if (!overridePath.isNullOrBlank()) {
        File(overridePath).readText(Charsets.UTF_8)
    } else {
        val isr = this::class.java.classLoader.getResourceAsStream(classpathName)
            ?: error("Could not find $classpathName in resources (src/main/resources)")
        isr.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }
}

@OptIn(ExperimentalUuidApi::class)
fun Application.module() {
    val json = Json {
        prettyPrint = true
        encodeDefaults = true
        explicitNulls = false
    }

    install(ContentNegotiation) { json(json) }
    install(SSE)
    install(CORS) {
        allowHost("localhost:3000", schemes = listOf("http"))
        allowHost("127.0.0.1", schemes = listOf("http"))
        allowHost("suajili.vercel.app", schemes = listOf("https"))

        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)

        // optional, but often helpful
        allowNonSimpleContentTypes = true

        // If later you use cookies/auth:
        // allowCredentials = true
    }

    install(OpenApi) {
        schemas { generator = SchemaGenerator.kotlinx(json) }
        examples { exampleEncoder = ExampleEncoder.kotlinx(json) }
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            cause.printStackTrace()
            call.respondText(
                text = "Server error: ${cause::class.simpleName}: ${cause.message}",
                status = HttpStatusCode.BadRequest
            )
        }
    }

    install(Koog) {
        agentConfig {
            registerTools {
                tools(TripsTools(TripsRepository(loadViajesJsonText())))
            }
        }
    }

    routing {
        options { call.respond(HttpStatusCode.OK) }
        staticFiles("/static", File("static"))

        route("/api.json") { openApi() }
        route("/swagger") { swaggerUI("/api.json") }

        get("/health") { call.respondText("ok") }

        get("/messages/{conversationId}", {
            description = "Get message history"
            tags = listOf("chat")
            response {
                HttpStatusCode.OK to {
                    description = "Message list"
                    body<MessageListResponse>()
                }
            }
        }) {
            val conversationId = call.parameters["conversationId"]
            call.respond(
                MessageListResponse(
                    messages = history.getOrDefault(
                        conversationId,
                        emptyList()
                    )
                )
            )
        }

        post("/chat/{conversationId}", {
            description = "Append a user message"
            tags = listOf("chat")
            request {
                body<String> {
                    required = true
                }
            }
            response {
                HttpStatusCode.Created to { description = "Created" }
            }
        }) {
            val message = call.receiveText()
            val conversationId = call.parameters["conversationId"] ?: ""

            if (conversationId.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val userMessage = Message(role = Role.USER, content = message)
            history[conversationId] = history.getOrPut(conversationId, { mutableListOf() })
            history[conversationId]?.add(userMessage)

            call.application.launch {
                try {
                    val conversation = history[conversationId].orEmpty()
                    val input = buildAgentInput(conversation)

                    val answer = this@post.aiAgent(
                        input = input,
                        model = OpenAIModels.Chat.GPT4_1,
                        runMode = ToolCalls.SINGLE_RUN_SEQUENTIAL // default; explicit is fine
                    )

                    // Emit as "stream" (chunking)
                    chunkForStreaming(answer).forEach { chunk ->
                        ChatBus.emit(ChatStreamEvent.Frame(conversationId, StreamFrame.Append(chunk)))
                        delay(25)
                    }

                    // Persist assistant response
                    val message = Message(role = Role.ASSISTANT, content = answer)
                    history[conversationId]?.add(message)

                    ChatBus.emit(ChatStreamEvent.Frame(conversationId, StreamFrame.End()))
                } catch (t: Throwable) {
                    t.printStackTrace()
                    ChatBus.emit(
                        ChatStreamEvent.Frame(
                            conversationId,
                            StreamFrame.Append("\n\n❌ Error: ${t.message ?: "unknown"}")
                        )
                    )
                    ChatBus.emit(ChatStreamEvent.Frame(conversationId, StreamFrame.End()))
                }
            }

            call.respond<Message>(userMessage)
        }

        sse("/chat/stream") {
            // Helpful headers for proxies

            val conversationId = call.request.queryParameters["conversationId"] ?: "default"

            // 1) Send something immediately so Cloud Run + browser see bytes
            send(ServerSentEvent(event = "ready", data = "ok"))

            // 2) Keepalive pings (every 15s)
            val pingJob = launch {
                while (isActive) {
                    delay(15_000)
                    // data can be anything; keep it tiny
                    send(ServerSentEvent(event = "ping", data = "1"))
                }
            }

            try {
                val stream = ChatBus.stream(conversationId)
                stream.collect { ev ->
                    when (ev) {
                        is ChatStreamEvent.Frame -> {
                            val frame = ev.frame
                            println("Frame: $frame")

                            // 1) Text deltas
                            frame.asTextOrNull()?.let { text ->
                                val payload = json.encodeToString(DeltaPayload(text))
                                send(ServerSentEvent(event = "delta", data = payload))
                            }

                            // 2) Completion
                            if (frame is StreamFrame.End) {
                                send(ServerSentEvent(event = "done", data = "done"))
                            }

                            // 3) Optional: tool calls
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
            } catch (e: Throwable) {
                throw e
            }
            finally {
                pingJob.cancel()
            }
        }
    }
}