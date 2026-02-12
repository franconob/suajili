package com.francoherrero.ai_agent_multiplatform.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CatalogTripDto(
    val id: Int,
    val title: String,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("duration_days")
    val durationDays: Int,
    val route: List<String> = emptyList(),
    @SerialName("price_currency")
    val priceCurrency: String? = null,
    @SerialName("price_base")
    val priceBase: Double? = null,
    @SerialName("price_single_supplement")
    val priceSingleSupplement: Double? = null,
    @SerialName("price_taxes")
    val priceTaxes: Double? = null,
    val includes: List<String>? = null,
    @SerialName("not_included")
    val notIncluded: List<String>? = null,
    val requirements: List<String>? = null,
    val url: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
)

@Serializable
data class CatalogTripRequest(
    val id: Int? = null,
    val title: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("duration_days")
    val durationDays: Int? = null,
    val route: List<String>? = null,
    @SerialName("price_currency")
    val priceCurrency: String? = null,
    @SerialName("price_base")
    val priceBase: Double? = null,
    @SerialName("price_single_supplement")
    val priceSingleSupplement: Double? = null,
    @SerialName("price_taxes")
    val priceTaxes: Double? = null,
    val includes: List<String>? = null,
    @SerialName("not_included")
    val notIncluded: List<String>? = null,
    val requirements: List<String>? = null,
    val url: String? = null,
)
