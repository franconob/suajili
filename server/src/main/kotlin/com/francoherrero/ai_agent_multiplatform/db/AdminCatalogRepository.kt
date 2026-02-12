package com.francoherrero.ai_agent_multiplatform.db

import com.francoherrero.ai_agent_multiplatform.model.CatalogTripDto
import com.francoherrero.ai_agent_multiplatform.model.CatalogTripRequest
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.OffsetDateTime

class AdminCatalogRepository {

    private fun sortColumn(field: String?): Column<*> = when (field) {
        "title" -> CatalogTrips.title
        "duration_days" -> CatalogTrips.durationDays
        "price_base" -> CatalogTrips.priceBase
        "created_at" -> CatalogTrips.createdAt
        "updated_at" -> CatalogTrips.updatedAt
        else -> CatalogTrips.id
    }

    fun list(
        start: Long,
        end: Long,
        sortField: String?,
        sortOrder: String?,
    ): Pair<List<CatalogTripDto>, Long> = transaction {
        val total = CatalogTrips.selectAll().count()
        val order = if (sortOrder?.uppercase() == "DESC") SortOrder.DESC else SortOrder.ASC
        val limit = (end - start).toInt()
        val offset = start

        val items = CatalogTrips
            .selectAll()
            .orderBy(sortColumn(sortField), order)
            .limit(limit)
            .offset(offset)
            .map { it.toCatalogTripDto() }

        items to total
    }

    fun getById(id: Int): CatalogTripDto? = transaction {
        CatalogTrips
            .selectAll()
            .where { CatalogTrips.id eq id }
            .map { it.toCatalogTripDto() }
            .singleOrNull()
    }

    fun create(request: CatalogTripRequest): CatalogTripDto = transaction {
        val now = OffsetDateTime.now()
        val newId = request.id ?: error("id is required for create")

        CatalogTrips.insert {
            it[title] = request.title ?: error("title is required for create")
            it[imageUrl] = request.imageUrl
            it[durationDays] = request.durationDays ?: error("duration_days is required for create")
            it[route] = request.route ?: emptyList()
            it[priceCurrency] = request.priceCurrency
            it[priceBase] = request.priceBase?.toBigDecimal()
            it[priceSingleSupplement] = request.priceSingleSupplement?.toBigDecimal()
            it[priceTaxes] = request.priceTaxes?.toBigDecimal()
            it[includes] = request.includes
            it[notIncluded] = request.notIncluded
            it[requirements] = request.requirements
            it[url] = request.url
            it[createdAt] = now
            it[updatedAt] = now
        }

        CatalogTrips.selectAll()
            .where { CatalogTrips.id eq newId }
            .single()
            .toCatalogTripDto()
    }

    fun update(id: Int, request: CatalogTripRequest): CatalogTripDto? = transaction {
        val existing = CatalogTrips.selectAll()
            .where { CatalogTrips.id eq id }
            .singleOrNull() ?: return@transaction null

        CatalogTrips.update({ CatalogTrips.id eq id }) {
            request.title?.let { v -> it[title] = v }
            request.imageUrl?.let { v -> it[imageUrl] = v }
            request.durationDays?.let { v -> it[durationDays] = v }
            request.route?.let { v -> it[route] = v }
            request.priceCurrency?.let { v -> it[priceCurrency] = v }
            request.priceBase?.let { v -> it[priceBase] = v.toBigDecimal() }
            request.priceSingleSupplement?.let { v -> it[priceSingleSupplement] = v.toBigDecimal() }
            request.priceTaxes?.let { v -> it[priceTaxes] = v.toBigDecimal() }
            request.includes?.let { v -> it[includes] = v }
            request.notIncluded?.let { v -> it[notIncluded] = v }
            request.requirements?.let { v -> it[requirements] = v }
            request.url?.let { v -> it[url] = v }
            it[updatedAt] = OffsetDateTime.now()
        }

        CatalogTrips.selectAll()
            .where { CatalogTrips.id eq id }
            .single()
            .toCatalogTripDto()
    }

    fun delete(id: Int): CatalogTripDto? = transaction {
        val dto = CatalogTrips.selectAll()
            .where { CatalogTrips.id eq id }
            .map { it.toCatalogTripDto() }
            .singleOrNull() ?: return@transaction null

        CatalogTrips.deleteWhere { CatalogTrips.id eq id }
        dto
    }
}
