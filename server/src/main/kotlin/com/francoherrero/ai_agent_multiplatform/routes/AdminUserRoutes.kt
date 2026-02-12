package com.francoherrero.ai_agent_multiplatform.routes

import com.francoherrero.ai_agent_multiplatform.db.AdminUsersRepository
import com.francoherrero.ai_agent_multiplatform.model.UserDto
import com.francoherrero.ai_agent_multiplatform.model.UserRequest
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.patch
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.adminUserRoutes(repo: AdminUsersRepository) {
    authenticate("admin") {
        route("/users") {

            get({
                description = "List users (paginated)"
                response {
                    code(HttpStatusCode.OK) {
                        body<List<UserDto>>()
                    }
                }
            }) {
                val start = call.request.queryParameters["_start"]?.toLongOrNull() ?: 0
                val end = call.request.queryParameters["_end"]?.toLongOrNull() ?: 10
                val sortField = call.request.queryParameters["_sort"]
                val sortOrder = call.request.queryParameters["_order"]

                val (items, total) = repo.list(start, end, sortField, sortOrder)

                call.response.header("X-Total-Count", total.toString())
                call.response.header("Access-Control-Expose-Headers", "X-Total-Count")
                call.respond(items)
            }

            get("/{id}", {
                description = "Get a single user by ID"
                response {
                    code(HttpStatusCode.OK) {
                        body<UserDto>()
                    }
                }
            }) {
                val id = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing id")

                val user = repo.getById(UUID.fromString(id))
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Not found"))

                call.respond(user)
            }

            patch("/{id}", {
                description = "Update user profile"
                request {
                    body<UserRequest> { required = true }
                }
                response {
                    code(HttpStatusCode.OK) {
                        body<UserDto>()
                    }
                }
            }) {
                val id = call.parameters["id"]
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, "Missing id")

                val request = call.receive<UserRequest>()
                val updated = repo.update(UUID.fromString(id), request)
                    ?: return@patch call.respond(HttpStatusCode.NotFound, mapOf("error" to "Not found"))

                call.respond(updated)
            }

            delete("/{id}", {
                description = "Delete user profile"
                response {
                    code(HttpStatusCode.OK) {
                        body<UserDto>()
                    }
                }
            }) {
                val id = call.parameters["id"]
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing id")

                val deleted = repo.delete(UUID.fromString(id))
                    ?: return@delete call.respond(HttpStatusCode.NotFound, mapOf("error" to "Not found"))

                call.respond(deleted)
            }
        }
    }
}
