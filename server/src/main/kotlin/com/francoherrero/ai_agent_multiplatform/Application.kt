package com.francoherrero.ai_agent_multiplatform

import com.francoherrero.ai_agent_multiplatform.chat.ChatBus
import com.francoherrero.ai_agent_multiplatform.chat.ChatStreamEvent
import com.francoherrero.ai_agent_multiplatform.model.Message
import com.francoherrero.ai_agent_multiplatform.model.MessageListResponse
import com.francoherrero.ai_agent_multiplatform.model.Role
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
import io.ktor.server.engine.*
import io.ktor.server.http.content.staticFiles
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.SSE
import io.ktor.server.sse.sse
import io.ktor.sse.ServerSentEvent
import io.ktor.sse.ServerSentEventMetadata
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.collections.emptyList

var history: MutableMap<String, MutableList<Message>> = mutableMapOf()

fun main() {
    embeddedServer(
        Netty,
        port = SERVER_PORT,
        host = "0.0.0.0",
        module = Application::module,
        watchPaths = listOf("classes")
    )
        .start(wait = true)
}

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

        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)

        allowHeader(HttpHeaders.ContentType)

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

    routing {
        staticFiles("/static", File("static"))

        route("/api.json") { openApi() }
        route("/swagger") { swaggerUI("/api.json") }

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
                        emptyList<Message>()
                    )
                )
            )
        }

        post("/chat/{conversationId}", {
            description = "Append a user message"
            tags = listOf("chat")
            request {}
            response {
                HttpStatusCode.OK to { description = "Accepted" }
            }
        }) {
            val message = call.receive<Message>()
            val conversationId = call.parameters["conversationId"] ?: ""

            if (conversationId.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            history[conversationId] = history.getOrPut(conversationId, { mutableListOf() })
            history[conversationId]?.add(message)

            val chunks = listOf("Hola", " 👋", " esto", " es", " una", " respuesta", " simulada.")

            call.application.launch {
                for (chunk in chunks) {
                    ChatBus.emit(ChatStreamEvent.Delta(conversationId, chunk))
                    delay(100)
                }

                ChatBus.emit(ChatStreamEvent.Done(conversationId))
            }

            call.respond(HttpStatusCode.OK)
        }

        route({
            description = "Chat streaming output via Server-Sent Events"
            tags = listOf("chat", "sse")
            response {
                HttpStatusCode.OK to {
                    description = "Stream of messages"
                    body<ServerSentEvent>()
                }
            }
        }) {
            sse("/chat/stream") {
                val conversationId = call.request.queryParameters["conversationId"] ?: "default"

                val stream = ChatBus.stream(conversationId)

                stream.collect {
                    when (it) {
                        is ChatStreamEvent.Delta ->
                            send(ServerSentEvent(event = "delta", data = it.text))

                        is ChatStreamEvent.Done ->
                            send(ServerSentEvent(event = "done", data = "done"))
                    }
                }
            }
        }
    }
}