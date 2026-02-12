package com.francoherrero.ai_agent_multiplatform.routes

import com.francoherrero.ai_agent_multiplatform.db.AdminTripsRepository
import com.francoherrero.ai_agent_multiplatform.model.AdminTripRequest
import com.francoherrero.ai_agent_multiplatform.model.UserTripDto
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.patch
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.adminTripRoutes(repo: AdminTripsRepository) {
    authenticate("admin") {
        route("/trips") {

            get({
                description = "List trips (paginated)"
                response {
                    code(HttpStatusCode.OK) {
                        body<List<UserTripDto>>()
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
                description = "Get a single trip by ID"
                response {
                    code(HttpStatusCode.OK) {
                        body<UserTripDto>()
                    }
                }
            }) {
                val id = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing id")

                val trip = repo.getById(UUID.fromString(id))
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Not found"))

                call.respond(trip)
            }

            post({
                description = "Create a new trip"
                request {
                    body<AdminTripRequest> { required = true }
                }
                response {
                    HttpStatusCode.Created to {
                        body<UserTripDto>()
                    }
                }
            }) {
                val request = call.receive<AdminTripRequest>()
                val created = repo.create(request)
                call.respond(HttpStatusCode.Created, created)
            }

            patch("/{id}", {
                description = "Update an existing trip"
                request {
                    body<AdminTripRequest> { required = true }
                }
                response {
                    code(HttpStatusCode.OK) {
                        body<UserTripDto>()
                    }
                }
            }) {
                val id = call.parameters["id"]
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, "Missing id")

                val request = call.receive<AdminTripRequest>()
                val updated = repo.update(UUID.fromString(id), request)
                    ?: return@patch call.respond(HttpStatusCode.NotFound, mapOf("error" to "Not found"))

                call.respond(updated)
            }

            delete("/{id}", {
                description = "Delete a trip"
                response {
                    code(HttpStatusCode.OK) {
                        body<UserTripDto>()
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
