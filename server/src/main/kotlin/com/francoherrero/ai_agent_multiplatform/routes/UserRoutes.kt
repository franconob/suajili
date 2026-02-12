package com.francoherrero.ai_agent_multiplatform.routes

import com.francoherrero.ai_agent_multiplatform.db.UserTripsRepository
import com.francoherrero.ai_agent_multiplatform.repository.TripsRepository
import io.github.smiley4.ktoropenapi.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun Route.userRoutes(
    userTripsRepository: UserTripsRepository,
    tripsRepository: TripsRepository
) {
    authenticate("supabase") {
        route("/users/me") {

//            get("/trips") {
//                val principal = call.principal<JWTPrincipal>()!!
//                val userId = Uuid.parse(principal.subject!!)
//
//                val userTrips = userTripsRepository.listUserTrips(userId)
//
//                // Enrich with related data
//                val enrichedTrips = userTrips.map { userTrip ->
//                    val documents = userTripsRepository.getTripDocuments(UUID.fromString(userTrip.id))
//                    val travelers = userTripsRepository.getTripTravelers(UUID.fromString(userTrip.id))
//
//                    // For group trips, get catalog data
//                    val catalogTripId = userTrip.catalogTripId
//                    val (tripTitle, tripImageUrl, tripRoute, tripDurationDays) = if (userTrip.type == TripType.GROUP && catalogTripId != null) {
//                        val catalogTrip = tripsRepository.byTitle(catalogTripId)
//                        Triple4(catalogTrip?.title, catalogTrip?.imageUrl, catalogTrip?.route, catalogTrip?.durationDays)
//                    } else {
//                        Triple4(userTrip.title, null, null, null)
//                    }
//
//                    // For custom trips, get destinations
//                    val destinations = if (userTrip.type == TripType.CUSTOM) {
//                        userTripsRepository.getTripDestinations(UUID.fromString(userTrip.id))
//                    } else null
//
//                    userTrip.copy(
//                        tripTitle = tripTitle,
//                        tripImageUrl = tripImageUrl,
//                        tripRoute = tripRoute,
//                        tripDurationDays = tripDurationDays,
//                        destinations = destinations,
//                        travelers = travelers,
//                        documents = documents
//                    )
//                }
//
//                call.respond(enrichedTrips)
//            }
//
//            get("/trips/next") {
//                val principal = call.principal<JWTPrincipal>()!!
//                val userId = Uuid.parse(principal.subject!!)
//
//                val nextTrip = userTripsRepository.getNextUserTrip(userId)
//
//                if (nextTrip == null) {
//                    call.respond(HttpStatusCode.OK, mapOf("trip" to null))
//                    return@get
//                }
//
//                // Enrich with related data
//                val documents = userTripsRepository.getTripDocuments(UUID.fromString(nextTrip.id))
//                val travelers = userTripsRepository.getTripTravelers(UUID.fromString(nextTrip.id))
//
//                val nextTripCatalogId = nextTrip.catalogTripId
//                val (tripTitle, tripImageUrl, tripRoute, tripDurationDays) = if (nextTrip.type == TripType.GROUP && nextTripCatalogId != null) {
//                    val catalogTrip = tripsRepository.byTitle(nextTripCatalogId)
//                    Triple4(catalogTrip?.title, catalogTrip?.imageUrl, catalogTrip?.route, catalogTrip?.durationDays)
//                } else {
//                    Triple4(nextTrip.title, null, null, null)
//                }
//
//                val destinations = if (nextTrip.type == TripType.CUSTOM) {
//                    userTripsRepository.getTripDestinations(UUID.fromString(nextTrip.id))
//                } else null
//
//                val enrichedTrip = nextTrip.copy(
//                    tripTitle = tripTitle,
//                    tripImageUrl = tripImageUrl,
//                    tripRoute = tripRoute,
//                    tripDurationDays = tripDurationDays,
//                    destinations = destinations,
//                    travelers = travelers,
//                    documents = documents
//                )
//
//                call.respond(mapOf("trip" to enrichedTrip))
//            }
//
//            get("/trips/{tripId}") {
//                val principal = call.principal<JWTPrincipal>()!!
//                val userId = Uuid.parse(principal.subject!!)
//
//                val tripIdStr = call.parameters["tripId"]
//                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing tripId")
//
//                val tripId = runCatching { UUID.fromString(tripIdStr) }.getOrNull()
//                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid tripId format")
//
//                val userTrip = userTripsRepository.getUserTrip(userId, tripId)
//
//                if (userTrip == null) {
//                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Trip not found"))
//                    return@get
//                }
//
//                // Enrich with related data
//                val documents = userTripsRepository.getTripDocuments(tripId)
//                val travelers = userTripsRepository.getTripTravelers(tripId)
//
//                val userTripCatalogId = userTrip.catalogTripId
//                val (tripTitle, tripImageUrl, tripRoute, tripDurationDays) = if (userTrip.type == TripType.GROUP && userTripCatalogId != null) {
//                    val catalogTrip = tripsRepository.byTitle(userTripCatalogId)
//                    Triple4(catalogTrip?.title, catalogTrip?.imageUrl, catalogTrip?.route, catalogTrip?.durationDays)
//                } else {
//                    Triple4(userTrip.title, null, null, null)
//                }
//
//                val destinations = if (userTrip.type == TripType.CUSTOM) {
//                    userTripsRepository.getTripDestinations(tripId)
//                } else null
//
//                val enrichedTrip = userTrip.copy(
//                    tripTitle = tripTitle,
//                    tripImageUrl = tripImageUrl,
//                    tripRoute = tripRoute,
//                    tripDurationDays = tripDurationDays,
//                    destinations = destinations,
//                    travelers = travelers,
//                    documents = documents
//                )
//
//                call.respond(enrichedTrip)
//            }

            get("/trips/{tripId}/travelers") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = Uuid.parse(principal.subject!!)

                val tripIdStr = call.parameters["tripId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing tripId")

                val tripId = runCatching { UUID.fromString(tripIdStr) }.getOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid tripId format")

                // Verify user owns this trip
                val userTrip = userTripsRepository.getUserTrip(userId, tripId)
                if (userTrip == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Trip not found"))
                    return@get
                }

                val travelers = userTripsRepository.getTripTravelers(tripId)
                call.respond(travelers)
            }

            get("/trips/{tripId}/documents") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = Uuid.parse(principal.subject!!)

                val tripIdStr = call.parameters["tripId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing tripId")

                val tripId = runCatching { UUID.fromString(tripIdStr) }.getOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid tripId format")

                // Verify user owns this trip
                val userTrip = userTripsRepository.getUserTrip(userId, tripId)
                if (userTrip == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Trip not found"))
                    return@get
                }

                val documents = userTripsRepository.getTripDocuments(tripId)
                call.respond(documents)
            }
        }
    }
}

// Helper data class for destructuring
private data class Triple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
