@file:OptIn(ExperimentalUuidApi::class)

package com.francoherrero.ai_agent_multiplatform.db

import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone
import kotlin.uuid.ExperimentalUuidApi


object Conversations : UUIDTable("conversations") {
    val title = text("title").nullable()
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")
}

object Messages : UUIDTable("messages") {
    val conversationId = reference("conversation_id", Conversations.id)
    val role = varchar("role", 16) // USER / ASSISTANT / SYSTEM
    val content = text("content")
    val clientMessageId = varchar("client_message_id", 128).nullable()
    val status = varchar("status", 16) // sent / streaming / done / failed
    val createdAt = timestampWithTimeZone("created_at")
}

// ============================================
// CATALOG TABLES
// ============================================

object CatalogTrips : org.jetbrains.exposed.v1.core.Table("catalog_trips") {
    val id = varchar("id", 255)
    val title = varchar("title", 255)
    val imageUrl = text("image_url").nullable()
    val durationDays = integer("duration_days")
    val priceCurrency = varchar("price_currency", 3).nullable()
    val priceBase = decimal("price_base", 10, 2).nullable()
    val priceSingleSupplement = decimal("price_single_supplement", 10, 2).nullable()
    val priceTaxes = decimal("price_taxes", 10, 2).nullable()
    val url = text("url").nullable()
    val embeddingText = text("embedding_text").nullable()
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")

    override val primaryKey = PrimaryKey(id)
}

object CatalogTripDepartures : UUIDTable("catalog_trip_departures") {
    val catalogTripId = varchar("catalog_trip_id", 255).references(CatalogTrips.id)
    val departureDate = date("departure_date")
    val returnDate = date("return_date").nullable()
    val maxParticipants = integer("max_participants").nullable()
    val currentParticipants = integer("current_participants").default(0)
    val status = varchar("status", 32).default("open")
    val createdAt = timestampWithTimeZone("created_at")
}

// ============================================
// USER BOOKING TABLES
// ============================================

object UserTrips : UUIDTable("user_trips") {
    val userId = uuid("user_id")
    val type = varchar("type", 32).default("group") // group | custom
    val catalogTripId = reference("catalog_trip_id", CatalogTrips.id)
    val departureId = reference("departure_id", CatalogTripDepartures.id).nullable()
    val title = varchar("title", 255).nullable()
    val status = varchar("status", 32).default("pending") // pending, confirmed, paid, completed, cancelled
    val departureDate = date("departure_date").nullable()
    val returnDate = date("return_date").nullable()
    val totalPrice = decimal("total_price", 10, 2).nullable()
    val currency = varchar("currency", 3).nullable()
    val notes = text("notes").nullable()
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")
}

object UserTripDestinations : UUIDTable("user_trip_destinations") {
    val userTripId = reference("user_trip_id", UserTrips.id)
    val destinationName = varchar("destination_name", 255)
    val country = varchar("country", 100).nullable()
    val sequence = integer("sequence")
    val startDate = date("start_date").nullable()
    val endDate = date("end_date").nullable()
    val nights = integer("nights").nullable()
    val accommodation = text("accommodation").nullable()
    val notes = text("notes").nullable()
    val createdAt = timestampWithTimeZone("created_at")
}

object UserTripTravelers : UUIDTable("user_trip_travelers") {
    val userTripId = reference("user_trip_id", UserTrips.id)
    val fullName = varchar("full_name", 255)
    val email = varchar("email", 255).nullable()
    val phone = varchar("phone", 50).nullable()
    val birthDate = date("birth_date").nullable()
    val nationality = varchar("nationality", 100).nullable()
    val documentType = varchar("document_type", 32).nullable() // passport, dni, other
    val documentNumber = varchar("document_number", 100).nullable()
    val documentExpiry = date("document_expiry").nullable()
    val isPrimary = bool("is_primary").default(false)
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")
}

object TripDocuments : UUIDTable("trip_documents") {
    val userTripId = reference("user_trip_id", UserTrips.id)
    val name = varchar("name", 255)
    val type = varchar("type", 32) // ticket, voucher, itinerary, insurance, passport, visa, other
    val storagePath = varchar("storage_path", 512)
    val fileSize = integer("file_size").nullable()
    val mimeType = varchar("mime_type", 100).nullable()
    val travelerId = reference("traveler_id", UserTripTravelers.id).nullable()
    val createdAt = timestampWithTimeZone("created_at")
}
