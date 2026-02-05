package com.francoherrero.ai_agent_multiplatform.routes

import com.francoherrero.ai_agent_multiplatform.auth.AuthResponse
import com.francoherrero.ai_agent_multiplatform.auth.AuthResult
import com.francoherrero.ai_agent_multiplatform.auth.SupabaseAuthService
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestBody(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequestBody(
    val email: String,
    val password: String,
    val fullName: String? = null
)

@Serializable
data class RefreshTokenRequestBody(
    val refreshToken: String
)

@Serializable
data class LogoutRequestBody(
    val accessToken: String
)

@Serializable
data class ErrorResponse(
    val error: String,
    val code: Int? = null
)

fun Route.authRoutes(authService: SupabaseAuthService) {
    route("/auth") {
        post("/login", {
            request {
                body<LoginRequestBody> {
                    required = true
                }
            }
            response {
                HttpStatusCode.OK to {
                    body<AuthResponse>()
                }
            }
        }) {
            val body = call.receive<LoginRequestBody>()

            when (val result = authService.login(body.email, body.password)) {
                is AuthResult.Success -> call.respond(HttpStatusCode.OK, result.data)
                is AuthResult.Error -> call.respond(
                    HttpStatusCode.fromValue(result.code ?: 401),
                    ErrorResponse(result.message, result.code)
                )
            }
        }

        post("/register") {
            val body = call.receive<RegisterRequestBody>()

            when (val result = authService.register(body.email, body.password, body.fullName)) {
                is AuthResult.Success -> call.respond(HttpStatusCode.Created, result.data)
                is AuthResult.Error -> call.respond(
                    HttpStatusCode.fromValue(result.code ?: 400),
                    ErrorResponse(result.message, result.code)
                )
            }
        }

        post("/refresh") {
            val body = call.receive<RefreshTokenRequestBody>()

            when (val result = authService.refreshToken(body.refreshToken)) {
                is AuthResult.Success -> call.respond(HttpStatusCode.OK, result.data)
                is AuthResult.Error -> call.respond(
                    HttpStatusCode.fromValue(result.code ?: 401),
                    ErrorResponse(result.message, result.code)
                )
            }
        }

        post("/logout") {
            val body = call.receive<LogoutRequestBody>()

            when (val result = authService.logout(body.accessToken)) {
                is AuthResult.Success -> call.respond(HttpStatusCode.NoContent)
                is AuthResult.Error -> call.respond(
                    HttpStatusCode.fromValue(result.code ?: 400),
                    ErrorResponse(result.message, result.code)
                )
            }
        }
    }
}
