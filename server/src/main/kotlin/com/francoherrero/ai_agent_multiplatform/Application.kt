package com.francoherrero.ai_agent_multiplatform

import ai.koog.agents.core.tools.reflect.tools
import ai.koog.ktor.Koog
import ai.koog.prompt.streaming.StreamFrame
import com.francoherrero.ai_agent_multiplatform.ai.TripsTools
import com.francoherrero.ai_agent_multiplatform.db.ChatRepository
import com.francoherrero.ai_agent_multiplatform.db.DatabaseFactory
import com.francoherrero.ai_agent_multiplatform.repository.TripsRepository
import com.francoherrero.ai_agent_multiplatform.routes.chatRoutes
import io.github.smiley4.ktoropenapi.OpenApi
import io.github.smiley4.ktoropenapi.config.ExampleEncoder
import io.github.smiley4.ktoropenapi.config.SchemaGenerator
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktoropenapi.route
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.http.content.staticFiles
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.options
import io.ktor.server.routing.routing
import io.ktor.server.sse.SSE
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.uuid.ExperimentalUuidApi

fun StreamFrame.asTextOrNull(): String? =
    when (this) {
        is StreamFrame.Append -> this.text
        else -> null
    }

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

    DatabaseFactory.init()
    val repo = ChatRepository()

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

        chatRoutes(repo)
    }
}