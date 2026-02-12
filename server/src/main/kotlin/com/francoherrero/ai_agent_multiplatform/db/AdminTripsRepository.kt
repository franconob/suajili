@file:OptIn(ExperimentalUuidApi::class)

package com.francoherrero.ai_agent_multiplatform.db

import com.francoherrero.ai_agent_multiplatform.model.AdminTripRequest
import com.francoherrero.ai_agent_multiplatform.model.UserTripDto
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

class AdminTripsRepository {

    private val joined
        get() = UserTrips.join(
            CatalogTrips,
            JoinType.LEFT,
            UserTrips.catalogTripId,
            CatalogTrips.id
        ).join(AuthUsers, JoinType.LEFT, UserTrips.userId, AuthUsers.id)
            .join(UserProfiles, JoinType.INNER, AuthUsers.id, UserProfiles.userId)

    private fun sortColumn(field: String?): Column<*> = when (field) {
        "status" -> UserTrips.status
        "departure_date" -> UserTrips.departureDate
        "return_date" -> UserTrips.returnDate
        "user_id" -> UserTrips.userId
        "type" -> UserTrips.type
        "total_price" -> UserTrips.totalPrice
        "created_at" -> UserTrips.createdAt
        "updated_at" -> UserTrips.updatedAt
        else -> UserTrips.id
    }

    fun list(
        start: Long,
        end: Long,
        sortField: String?,
        sortOrder: String?,
    ): Pair<List<UserTripDto>, Long> = transaction {
        val total = UserTrips.selectAll().count()
        val order = if (sortOrder?.uppercase() == "DESC") SortOrder.DESC else SortOrder.ASC
        val limit = (end - start).toInt()
        val offset = start

        val items = joined
            .selectAll()
            .orderBy(sortColumn(sortField), order)
            .limit(limit)
            .offset(offset)
            .map { it.toAdminTripDto() }

        items to total
    }

    fun getById(id: UUID): UserTripDto? = transaction {
        joined
            .selectAll()
            .where { UserTrips.id eq id }
            .map { it.toAdminTripDto() }
            .singleOrNull()
    }

    fun create(request: AdminTripRequest): UserTripDto = transaction {
        val now = OffsetDateTime.now()
        val userId = UUID.fromString(request.userId ?: error("user_id is required"))

        val newId = UserTrips.insert {
            it[UserTrips.userId] = userId.toKotlinUuid()
            it[type] = (request.type?.name?.lowercase()) ?: "group"
            it[catalogTripId] = request.catalogTripId
            it[departureId] = request.departureId?.let { v -> UUID.fromString(v) }
            it[title] = request.title
            it[status] = request.status?.name?.lowercase() ?: "pending"
            it[departureDate] = request.departureDate?.let { v -> LocalDate.parse(v) }
            it[returnDate] = request.returnDate?.let { v -> LocalDate.parse(v) }
            it[totalPrice] = request.totalPrice?.toBigDecimal()
            it[currency] = request.currency
            it[notes] = request.notes
            it[createdAt] = now
            it[updatedAt] = now
        } get UserTrips.id

        joined
            .selectAll()
            .where { UserTrips.id eq newId.value }
            .single()
            .toAdminTripDto()
    }

    fun update(id: UUID, request: AdminTripRequest): UserTripDto? = transaction {
        val existing = UserTrips.selectAll()
            .where { UserTrips.id eq id }
            .singleOrNull() ?: return@transaction null

        UserTrips.update({ UserTrips.id eq id }) {
            request.userId?.let { v -> it[userId] = UUID.fromString(v).toKotlinUuid() }
            request.type?.let { v -> it[type] = v.name.lowercase() }
            request.catalogTripId?.let { v -> it[catalogTripId] = v }
            request.departureId?.let { v -> it[departureId] = UUID.fromString(v) }
            request.title?.let { v -> it[title] = v }
            request.status?.let { v -> it[status] = v.name.lowercase() }
            request.departureDate?.let { v -> it[departureDate] = LocalDate.parse(v) }
            request.returnDate?.let { v -> it[returnDate] = LocalDate.parse(v) }
            request.totalPrice?.let { v -> it[totalPrice] = v.toBigDecimal() }
            request.currency?.let { v -> it[currency] = v }
            request.notes?.let { v -> it[notes] = v }
            it[updatedAt] = OffsetDateTime.now()
        }

        joined
            .selectAll()
            .where { UserTrips.id eq id }
            .single()
            .toAdminTripDto()
    }

    fun delete(id: UUID): UserTripDto? = transaction {
        val dto = joined
            .selectAll()
            .where { UserTrips.id eq id }
            .map { it.toAdminTripDto() }
            .singleOrNull() ?: return@transaction null

        UserTrips.deleteWhere { UserTrips.id eq id }
        dto
    }
}
