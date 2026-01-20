package com.francoherrero.ai_agent_multiplatform.model

import kotlinx.serialization.Serializable

@Serializable
data class Trip(
    val id: String,
    val url: String? = null,
    val title: String,
    val imageUrl: String? = null,

    val durationDays: Int? = null,
    val route: List<String> = emptyList(),

    val nextDepartureIso: String? = null,
    val departureDatesIso: List<String> = emptyList(),

    val nights: Nights? = null,
    val mealsIncluded: MealsIncluded? = null,
    val price: Price? = null,

    val includes: List<String> = emptyList(),
    val notIncluded: List<String> = emptyList(),
    val requirements: List<String> = emptyList(),
    val embeddingText: String? = null,
)

@Serializable data class Nights(val hotel: Int? = null, val cruise: Int? = null, val total: Int? = null)
@Serializable data class MealsIncluded(val total: Int? = null, val lunch: Int? = null, val dinner: Int? = null)

@Serializable
data class Price(
    val currency: String? = null,
    val basePerPersonDouble: Double? = null,
    val singleSupplement: Double? = null,
    val taxesApprox: Double? = null,
    val raw: String? = null,
)

@Serializable
data class TripHit(
    val id: String,
    val title: String,
    val url: String? = null,
    val imageUrl: String? = null,
    val durationDays: Int? = null,
    val nextDepartureIso: String? = null,
    val route: List<String> = emptyList(),
    val currency: String? = null,
    val basePerPersonDouble: Double? = null,
)

@Serializable
data class SearchTripsArgs(
    val q: String? = null,
    val routeContains: String? = null,
    val departureFromIso: String? = null, // "2026-01-01"
    val departureToIso: String? = null,   // "2026-12-31"
    val maxPrice: Double? = null,
    val currency: String? = null,
    val limit: Int = 5,
)

@Serializable
data class SearchTripsResult(
    val hits: List<TripHit> = emptyList()
)

data class SearchTripsRequest(
    val q: String? = null,
    val dateFromIso: String? = null, // later you can parse to LocalDate
    val dateToIso: String? = null,
    val maxPrice: Double? = null,
    val currency: String? = null,
    val limit: Int = 5,
)
