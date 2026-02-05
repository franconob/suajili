package com.francoherrero.ai_agent_multiplatform.trips

import com.francoherrero.ai_agent_multiplatform.api.ApiResult
import com.francoherrero.ai_agent_multiplatform.model.NextTripResponse
import com.francoherrero.ai_agent_multiplatform.model.TripDocumentDto
import com.francoherrero.ai_agent_multiplatform.model.UserTripDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode

class UserTripsApi(
    private val client: HttpClient,
) {

    suspend fun getMyTrips(): ApiResult<List<UserTripDto>> {

        return try {
            val response = client.get("/users/me/trips")

            if (response.status == HttpStatusCode.OK) {
                val trips = response.body<List<UserTripDto>>()
                ApiResult.Ok(trips)
            } else {
                ApiResult.Error("Failed to fetch trips", response.status.value)
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error", null)
        }
    }

    suspend fun getNextTrip(): ApiResult<UserTripDto?> {
        return try {
            val response = client.get("/users/me/trips/next")

            if (response.status == HttpStatusCode.OK) {
                val nextTripResponse = response.body<NextTripResponse>()
                ApiResult.Ok(nextTripResponse.trip)
            } else {
                ApiResult.Error("Failed to fetch next trip", response.status.value)
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error", null)
        }
    }

    suspend fun getTrip(tripId: String): ApiResult<UserTripDto> {
        return try {
            val response = client.get("/users/me/trips/$tripId")
            when (response.status) {
                HttpStatusCode.OK -> {
                    val trip = response.body<UserTripDto>()
                    ApiResult.Ok(trip)
                }
                HttpStatusCode.NotFound -> {
                    ApiResult.Error("Trip not found", 404)
                }
                else -> {
                    ApiResult.Error("Failed to fetch trip", response.status.value)
                }
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error", null)
        }
    }

    suspend fun getTripDocuments(tripId: String): ApiResult<List<TripDocumentDto>> {
        return try {
            val response = client.get("/users/me/trips/$tripId/documents")

            if (response.status == HttpStatusCode.OK) {
                val documents = response.body<List<TripDocumentDto>>()
                ApiResult.Ok(documents)
            } else {
                ApiResult.Error("Failed to fetch documents", response.status.value)
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error", null)
        }
    }
}
