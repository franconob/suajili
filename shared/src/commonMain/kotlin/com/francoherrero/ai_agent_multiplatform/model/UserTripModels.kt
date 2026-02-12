package com.francoherrero.ai_agent_multiplatform.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============================================
// ENUMS
// ============================================

@Serializable
enum class TripType {
    @SerialName("group") GROUP,
    @SerialName("custom") CUSTOM
}

@Serializable
enum class TripStatus {
    @SerialName("pending") PENDING,
    @SerialName("confirmed") CONFIRMED,
    @SerialName("paid") PAID,
    @SerialName("completed") COMPLETED,
    @SerialName("cancelled") CANCELLED
}

@Serializable
enum class DocumentType {
    @SerialName("ticket") TICKET,
    @SerialName("voucher") VOUCHER,
    @SerialName("itinerary") ITINERARY,
    @SerialName("insurance") INSURANCE,
    @SerialName("passport") PASSPORT,
    @SerialName("visa") VISA,
    @SerialName("other") OTHER
}

@Serializable
enum class TravelerDocumentType {
    @SerialName("passport") PASSPORT,
    @SerialName("dni") DNI,
    @SerialName("other") OTHER
}

// ============================================
// USER TRIP (Booking)
// ============================================

@Serializable
data class UserTripDto(
    val id: String,
    @SerialName("user_id")
    val userId: String,

    @SerialName("user_name")
    val userName: String,

    // Trip type
    val type: TripType = TripType.GROUP,

    // For group trips: reference to catalog
    @SerialName("catalog_trip_id")
    val catalogTripId: Int? = null,
    @SerialName("departure_id")
    val departureId: String? = null,

    // For custom trips (or override)
    val title: String? = null,

    // Booking details
    val status: TripStatus = TripStatus.PENDING,
    @SerialName("departure_date")
    val departureDate: String? = null,
    @SerialName("return_date")
    val returnDate: String? = null,

    // Pricing
    @SerialName("total_price")
    val totalPrice: Double? = null,
    val currency: String? = null,

    val notes: String? = null,

    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,

    // ---- Joined/enriched data (not in DB) ----

    // From catalog_trips (for group trips)
    @SerialName("trip_title")
    val tripTitle: String? = null,
    @SerialName("trip_image_url")
    val tripImageUrl: String? = null,
    @SerialName("trip_route")
    val tripRoute: List<String>? = null,
    @SerialName("trip_duration_days")
    val tripDurationDays: Int? = null,

    // Related entities
    val destinations: List<UserTripDestinationDto>? = null,
    val travelers: List<TravelerDto>? = null,
    val documents: List<TripDocumentDto>? = null
)

// ============================================
// CUSTOM TRIP DESTINATIONS
// ============================================

@Serializable
data class UserTripDestinationDto(
    val id: String,
    @SerialName("user_trip_id")
    val userTripId: String,
    @SerialName("destination_name")
    val destinationName: String,
    val country: String? = null,
    val sequence: Int,
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("end_date")
    val endDate: String? = null,
    val nights: Int? = null,
    val accommodation: String? = null,
    val notes: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)

// ============================================
// TRAVELERS
// ============================================

@Serializable
data class TravelerDto(
    val id: String,
    @SerialName("user_trip_id")
    val userTripId: String,

    // Personal info
    @SerialName("full_name")
    val fullName: String,
    val email: String? = null,
    val phone: String? = null,
    @SerialName("birth_date")
    val birthDate: String? = null,
    val nationality: String? = null,

    // Document
    @SerialName("document_type")
    val documentType: TravelerDocumentType? = null,
    @SerialName("document_number")
    val documentNumber: String? = null,
    @SerialName("document_expiry")
    val documentExpiry: String? = null,

    // Role
    @SerialName("is_primary")
    val isPrimary: Boolean = false,

    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

// ============================================
// DOCUMENTS
// ============================================

@Serializable
data class TripDocumentDto(
    val id: String,
    @SerialName("user_trip_id")
    val userTripId: String,

    val name: String,
    val type: DocumentType,

    @SerialName("storage_path")
    val storagePath: String,
    @SerialName("file_size")
    val fileSize: Int? = null,
    @SerialName("mime_type")
    val mimeType: String? = null,

    // Optional: link to specific traveler
    @SerialName("traveler_id")
    val travelerId: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null
)

// ============================================
// CATALOG TRIP DEPARTURE
// ============================================

@Serializable
data class CatalogTripDepartureDto(
    val id: String,
    @SerialName("catalog_trip_id")
    val catalogTripId: Int,
    @SerialName("departure_date")
    val departureDate: String,
    @SerialName("return_date")
    val returnDate: String? = null,
    @SerialName("max_participants")
    val maxParticipants: Int? = null,
    @SerialName("current_participants")
    val currentParticipants: Int? = null,
    val status: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)

// ============================================
// API RESPONSES
// ============================================

@Serializable
data class UserTripsResponse(
    val trips: List<UserTripDto>
)

@Serializable
data class NextTripResponse(
    val trip: UserTripDto?
)

// ============================================
// REQUEST BODIES (for creating/updating)
// ============================================

@Serializable
data class CreateUserTripRequest(
    val type: TripType = TripType.GROUP,
    @SerialName("catalog_trip_id")
    val catalogTripId: Int? = null,
    @SerialName("departure_id")
    val departureId: String? = null,
    val title: String? = null,
    @SerialName("departure_date")
    val departureDate: String? = null,
    @SerialName("return_date")
    val returnDate: String? = null,
    val notes: String? = null
)

@Serializable
data class AddTravelerRequest(
    @SerialName("full_name")
    val fullName: String,
    val email: String? = null,
    val phone: String? = null,
    @SerialName("birth_date")
    val birthDate: String? = null,
    val nationality: String? = null,
    @SerialName("document_type")
    val documentType: TravelerDocumentType? = null,
    @SerialName("document_number")
    val documentNumber: String? = null,
    @SerialName("document_expiry")
    val documentExpiry: String? = null,
    @SerialName("is_primary")
    val isPrimary: Boolean = false
)

@Serializable
data class AddDestinationRequest(
    @SerialName("destination_name")
    val destinationName: String,
    val country: String? = null,
    val sequence: Int,
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("end_date")
    val endDate: String? = null,
    val nights: Int? = null,
    val accommodation: String? = null,
    val notes: String? = null
)

// ============================================
// ADMIN REQUEST (for admin CRUD)
// ============================================

@Serializable
data class AdminTripRequest(
    @SerialName("user_id")
    val userId: String? = null,
    val type: TripType? = null,
    @SerialName("catalog_trip_id")
    val catalogTripId: Int? = null,
    @SerialName("departure_id")
    val departureId: String? = null,
    val title: String? = null,
    val status: TripStatus? = null,
    @SerialName("departure_date")
    val departureDate: String? = null,
    @SerialName("return_date")
    val returnDate: String? = null,
    @SerialName("total_price")
    val totalPrice: Double? = null,
    val currency: String? = null,
    val notes: String? = null,
)
