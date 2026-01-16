package com.francoherrero.ai_agent_multiplatform.model

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.concurrent.Volatile

class TripStore(
    private val store: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
) {
    @Volatile
    private var trips: List<Trip> = emptyList()

    fun loadFromJson(json: String) {
        trips = store.decodeFromString(ListSerializer(Trip.serializer()), json)
    }

    fun allTrips(): List<Trip> = trips

    fun isLoaded(): Boolean = trips.isNotEmpty()
}