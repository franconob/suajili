package com.francoherrero.ai_agent_multiplatform.model

import kotlinx.serialization.Serializable

@Serializable
data class Trip(
    val id: String,
    val url: String? = null,
    val title: String? = null,
    val imageUrl: String? = null,

    val durationDays: Int? = null,
    val route: List<String> = emptyList(),

    val nextDepartureIso: String? = null,
    val departureDatesIso: List<String> = emptyList(),

    val nights: Nights? = null,
    val mealsIncluded: MealsIncluded? = null,
    val price: Price? = null
)

@Serializable data class Nights(val hotel: Int? = null, val cruise: Int? = null, val total: Int? = null)
@Serializable data class MealsIncluded(val total: Int? = null, val lunch: Int? = null, val dinner: Int? = null)
@Serializable data class Price(val currency: String? = null, val basePerPersonDouble: Double? = null)