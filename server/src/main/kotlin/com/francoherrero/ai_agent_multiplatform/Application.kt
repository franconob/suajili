package com.francoherrero.ai_agent_multiplatform

import ai.koog.agents.core.tools.reflect.tools
import ai.koog.ktor.Koog
import ai.koog.prompt.streaming.StreamFrame
import com.francoherrero.ai_agent_multiplatform.ai.TripsTools
import com.francoherrero.ai_agent_multiplatform.auth.SupabaseAuthService
import com.francoherrero.ai_agent_multiplatform.db.AdminCatalogRepository
import com.francoherrero.ai_agent_multiplatform.db.ChatRepository
import com.francoherrero.ai_agent_multiplatform.db.DatabaseFactory
import com.francoherrero.ai_agent_multiplatform.repository.TripsRepository
import com.francoherrero.ai_agent_multiplatform.db.UserTripsRepository
import com.francoherrero.ai_agent_multiplatform.db.AdminTripsRepository
import com.francoherrero.ai_agent_multiplatform.db.AdminUsersRepository
import com.francoherrero.ai_agent_multiplatform.routes.adminCatalogRoutes
import com.francoherrero.ai_agent_multiplatform.routes.adminTripRoutes
import com.francoherrero.ai_agent_multiplatform.routes.adminUserRoutes
import com.francoherrero.ai_agent_multiplatform.routes.authRoutes
import com.francoherrero.ai_agent_multiplatform.routes.chatRoutes
import com.francoherrero.ai_agent_multiplatform.routes.userRoutes
import com.auth0.jwk.JwkProviderBuilder
import java.util.concurrent.TimeUnit
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
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
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
    val userTripsRepo = UserTripsRepository()
    val adminCatalogRepo = AdminCatalogRepository()
    val adminUsersRepo = AdminUsersRepository()
    val adminTripsRepo = AdminTripsRepository()

    // Supabase configuration from environment
    val supabaseUrl = System.getenv("SUPABASE_URL") ?: error("SUPABASE_URL not set")
    val supabaseAnonKey = System.getenv("SUPABASE_ANON_KEY") ?: error("SUPABASE_ANON_KEY not set")

    val authService = SupabaseAuthService(supabaseUrl, supabaseAnonKey)

    // JWKS provider for ES256 token verification
    val jwkProvider = JwkProviderBuilder(java.net.URL("$supabaseUrl/auth/v1/.well-known/jwks.json"))
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    install(ContentNegotiation) { json(json) }
    install(SSE)
    install(CORS) {
        allowHost("localhost:3000", schemes = listOf("http"))
        allowHost("10.0.2.2", schemes = listOf("http"))
        allowHost("localhost", schemes = listOf("http"))
        allowHost("suajili.vercel.app", schemes = listOf("https"))

        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)

        exposeHeader("X-Total-Count")

        allowNonSimpleContentTypes = true
    }

    install(Authentication) {
        jwt("supabase") {
            verifier(jwkProvider) {
                withAudience("authenticated")
                withIssuer("$supabaseUrl/auth/v1")
            }
            validate { credential ->
                val userId = credential.payload.subject
                if (userId != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid or expired token"))
            }
        }

        jwt("admin") {
            verifier(jwkProvider) {
                withAudience("authenticated")
                withIssuer("$supabaseUrl/auth/v1")
            }
            validate { credential ->
                val userId = credential.payload.subject
                val role = credential.payload.getClaim("user_role")?.asString()
                if (userId != null && role == "admin") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Admin access required"))
            }
        }
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

        authRoutes(authService)
        chatRoutes(repo)
        userRoutes(userTripsRepo, TripsRepository(loadViajesJsonText()))
        adminCatalogRoutes(adminCatalogRepo)
        adminUserRoutes(adminUsersRepo)
        adminTripRoutes(adminTripsRepo)
    }
}