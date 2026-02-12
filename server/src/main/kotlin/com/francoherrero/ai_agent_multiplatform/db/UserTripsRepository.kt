package com.francoherrero.ai_agent_multiplatform.db

import com.francoherrero.ai_agent_multiplatform.model.*
import org.jetbrains.exposed.v1.core.SortOrder.ASC
import org.jetbrains.exposed.v1.core.SortOrder.DESC
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.LocalDate
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class UserTripsRepository {

    // ============================================
    // USER TRIPS
    // ============================================

    fun listUserTrips(userId: Uuid): List<UserTripDto> = transaction {
        UserTrips
            .selectAll()
            .where { UserTrips.userId eq userId }
            .orderBy(UserTrips.departureDate, ASC)
            .map { row -> row.toUserTripDto() }
    }

    fun getUserTrip(userId: Uuid, tripId: UUID): UserTripDto? = transaction {
        UserTrips
            .selectAll()
            .where { (UserTrips.userId eq userId) and (UserTrips.id eq tripId) }
            .map { row -> row.toUserTripDto() }
            .singleOrNull()
    }

    fun getNextUserTrip(userId: Uuid): UserTripDto? = transaction {
        val now = LocalDate.now()
        UserTrips
            .selectAll()
            .where {
                (UserTrips.userId eq userId) and
                (UserTrips.departureDate greaterEq now) and
                (UserTrips.status neq "cancelled")
            }
            .orderBy(UserTrips.departureDate, ASC)
            .limit(1)
            .map { row -> row.toUserTripDto() }
            .singleOrNull()
    }

    // ============================================
    // DESTINATIONS (for custom trips)
    // ============================================

    fun getTripDestinations(userTripId: UUID): List<UserTripDestinationDto> = transaction {
        UserTripDestinations
            .selectAll()
            .where { UserTripDestinations.userTripId eq userTripId }
            .orderBy(UserTripDestinations.sequence, ASC)
            .map { row ->
                UserTripDestinationDto(
                    id = row[UserTripDestinations.id].value.toString(),
                    userTripId = row[UserTripDestinations.userTripId].toString(),
                    destinationName = row[UserTripDestinations.destinationName],
                    country = row[UserTripDestinations.country],
                    sequence = row[UserTripDestinations.sequence],
                    startDate = row[UserTripDestinations.startDate]?.toString(),
                    endDate = row[UserTripDestinations.endDate]?.toString(),
                    nights = row[UserTripDestinations.nights],
                    accommodation = row[UserTripDestinations.accommodation],
                    notes = row[UserTripDestinations.notes],
                    createdAt = row[UserTripDestinations.createdAt]?.toString()
                )
            }
    }

    // ============================================
    // TRAVELERS
    // ============================================

    fun getTripTravelers(userTripId: UUID): List<TravelerDto> = transaction {
        UserTripTravelers
            .selectAll()
            .where { UserTripTravelers.userTripId eq userTripId }
            .orderBy(UserTripTravelers.isPrimary, DESC)
            .map { row ->
                TravelerDto(
                    id = row[UserTripTravelers.id].value.toString(),
                    userTripId = row[UserTripTravelers.userTripId].toString(),
                    fullName = row[UserTripTravelers.fullName],
                    email = row[UserTripTravelers.email],
                    phone = row[UserTripTravelers.phone],
                    birthDate = row[UserTripTravelers.birthDate]?.toString(),
                    nationality = row[UserTripTravelers.nationality],
                    documentType = row[UserTripTravelers.documentType]?.let {
                        TravelerDocumentType.valueOf(it.uppercase())
                    },
                    documentNumber = row[UserTripTravelers.documentNumber],
                    documentExpiry = row[UserTripTravelers.documentExpiry]?.toString(),
                    isPrimary = row[UserTripTravelers.isPrimary],
                    createdAt = row[UserTripTravelers.createdAt]?.toString(),
                    updatedAt = row[UserTripTravelers.updatedAt]?.toString()
                )
            }
    }

    // ============================================
    // DOCUMENTS
    // ============================================

    fun getTripDocuments(userTripId: UUID): List<TripDocumentDto> = transaction {
        TripDocuments
            .selectAll()
            .where { TripDocuments.userTripId eq userTripId }
            .orderBy(TripDocuments.createdAt, DESC)
            .map { row ->
                TripDocumentDto(
                    id = row[TripDocuments.id].value.toString(),
                    userTripId = row[TripDocuments.userTripId].toString(),
                    name = row[TripDocuments.name],
                    type = DocumentType.valueOf(row[TripDocuments.type].uppercase()),
                    storagePath = row[TripDocuments.storagePath],
                    fileSize = row[TripDocuments.fileSize],
                    mimeType = row[TripDocuments.mimeType],
                    travelerId = row[TripDocuments.travelerId]?.toString(),
                    createdAt = row[TripDocuments.createdAt]?.toString()
                )
            }
    }

    // ============================================
    // HELPER: Row to DTO mapping
    // ============================================

    private fun org.jetbrains.exposed.v1.core.ResultRow.toUserTripDto(): UserTripDto {
        return UserTripDto(
            id = this[UserTrips.id].value.toString(),
            userId = this[UserTrips.userId].toString(),
            type = TripType.valueOf(this[UserTrips.type].uppercase()),
            catalogTripId = this[UserTrips.catalogTripId]?.value,
            departureId = this[UserTrips.departureId]?.toString(),
            title = this[UserTrips.title],
            status = TripStatus.valueOf(this[UserTrips.status].uppercase()),
            departureDate = this[UserTrips.departureDate]?.toString(),
            returnDate = this[UserTrips.returnDate]?.toString(),
            totalPrice = this[UserTrips.totalPrice]?.toDouble(),
            currency = this[UserTrips.currency],
            notes = this[UserTrips.notes],
            createdAt = this[UserTrips.createdAt].toString(),
            updatedAt = this[UserTrips.updatedAt].toString(),
            userName = this[UserProfiles.firstName].toString()
        )
    }
}
