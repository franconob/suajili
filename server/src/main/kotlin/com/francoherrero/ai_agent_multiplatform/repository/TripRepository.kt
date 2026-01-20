package com.francoherrero.ai_agent_multiplatform.repository

import com.francoherrero.ai_agent_multiplatform.model.SearchTripsRequest
import com.francoherrero.ai_agent_multiplatform.model.Trip
import com.francoherrero.ai_agent_multiplatform.model.TripHit
import kotlinx.serialization.json.Json


class TripsRepository(private val jsonString: String) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val trips: List<Trip> by lazy {
        json.decodeFromString<List<Trip>>(jsonString)
    }

    fun all(): List<Trip> = trips
    fun byTitle(title: String): Trip? = trips.firstOrNull { it.title.equals(title, ignoreCase = true) }
}

fun TripsRepository.search(req: SearchTripsRequest): List<TripHit> {
    val q = req.q?.trim()?.lowercase()

    return all()
        .asSequence()
        .filter { t ->
            val hay = buildString {
                append(t.title).append(' ')
                append(t.embeddingText ?: "").append(' ')
                append(t.route.joinToString(" "))
            }.lowercase()

            val okQ = q.isNullOrBlank() || hay.contains(q)

            val okCurrency = req.currency.isNullOrBlank() ||
                    t.price?.currency.equals(req.currency, ignoreCase = true)

            val okMaxPrice = req.maxPrice == null ||
                    ((t.price?.basePerPersonDouble ?: Double.POSITIVE_INFINITY) <= req.maxPrice!!)

            val next = t.nextDepartureIso
            val okFrom = req.dateFromIso.isNullOrBlank() || (next != null && next >= req.dateFromIso!!)
            val okTo = req.dateToIso.isNullOrBlank() || (next != null && next <= req.dateToIso!!)

            okQ && okCurrency && okMaxPrice && okFrom && okTo
        }
        .sortedBy { it.nextDepartureIso ?: "9999-12-31" }
        .take(req.limit.coerceIn(1, 20))
        .map { t ->
            TripHit(
                id = t.id,
                title = t.title,
                url = t.url,
                imageUrl = t.imageUrl,
                durationDays = t.durationDays,
                nextDepartureIso = t.nextDepartureIso,
                route = t.route,
                currency = t.price?.currency,
                basePerPersonDouble = t.price?.basePerPersonDouble,
            )
        }
        .toList()
}

