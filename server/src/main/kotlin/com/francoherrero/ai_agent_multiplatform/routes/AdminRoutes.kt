package com.francoherrero.ai_agent_multiplatform.routes

import com.francoherrero.ai_agent_multiplatform.db.AdminCatalogRepository
import com.francoherrero.ai_agent_multiplatform.model.CatalogTripDto
import com.francoherrero.ai_agent_multiplatform.model.CatalogTripRequest
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.patch
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.adminCatalogRoutes(repo: AdminCatalogRepository) {
    authenticate("admin") {
        route("/catalogs") {

            get({
                description = "List catalog trips (paginated)"
                response {
                    code(HttpStatusCode.OK) {
                        body<List<CatalogTripDto>>()
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
                description = "Get a single catalog trip by ID"
                response {
                    code(HttpStatusCode.OK) {
                        body<CatalogTripDto>()
                    }
                }
            }) {
                val id = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing id")

                val trip = repo.getById(id.toInt())
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Not found"))

                call.respond(trip)
            }

            post({
                description = "Create a new catalog trip"
                request {
                    body<CatalogTripRequest> { required = true }
                }
                response {
                    HttpStatusCode.Created to {
                        body<CatalogTripDto>()
                    }
                }
            }) {
                val request = call.receive<CatalogTripRequest>()
                val created = repo.create(request)
                call.respond(HttpStatusCode.Created, created)
            }

            patch("/{id}", {
                description = "Update an existing catalog trip"
                request {
                    body<CatalogTripRequest> { required = true }
                }
                response {
                    code(HttpStatusCode.OK) {
                        body<CatalogTripDto>()
                    }
                }
            }) {
                val id = call.parameters["id"]
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, "Missing id")

                val request = call.receive<CatalogTripRequest>()
                val updated = repo.update(id.toInt(), request)
                    ?: return@patch call.respond(HttpStatusCode.NotFound, mapOf("error" to "Not found"))

                call.respond(updated)
            }

            delete("/{id}", {
                description = "Delete a catalog trip"
                response {
                    code(HttpStatusCode.OK) {
                        body<CatalogTripDto>()
                    }
                }
            }) {
                val id = call.parameters["id"]
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing id")

                val deleted = repo.delete(id.toInt())
                    ?: return@delete call.respond(HttpStatusCode.NotFound, mapOf("error" to "Not found"))

                call.respond(deleted)
            }
        }
    }
}
