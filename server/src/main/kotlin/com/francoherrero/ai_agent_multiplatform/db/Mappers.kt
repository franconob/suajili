@file:OptIn(ExperimentalUuidApi::class)

package com.francoherrero.ai_agent_multiplatform.db

import com.francoherrero.ai_agent_multiplatform.model.CatalogTripDto
import com.francoherrero.ai_agent_multiplatform.model.ConversationDto
import com.francoherrero.ai_agent_multiplatform.model.MessageDto
import com.francoherrero.ai_agent_multiplatform.model.TripStatus
import com.francoherrero.ai_agent_multiplatform.model.TripType
import com.francoherrero.ai_agent_multiplatform.model.UserDto
import com.francoherrero.ai_agent_multiplatform.model.UserTripDto
import org.jetbrains.exposed.v1.core.ResultRow
import java.time.format.DateTimeFormatter
import kotlin.uuid.ExperimentalUuidApi

private val ISO = DateTimeFormatter.ISO_OFFSET_DATE_TIME

fun ResultRow.toConversationDto(): ConversationDto = ConversationDto(
    id = this[Conversations.id].value.toString(),
    title = this[Conversations.title],
    createdAt = ISO.format(this[Conversations.createdAt]),
    updatedAt = ISO.format(this[Conversations.updatedAt])
)

fun ResultRow.toMessageDto(): MessageDto = MessageDto(
    id = this[Messages.id].value.toString(),
    conversationId = this[Messages.conversationId].toString(),
    role = this[Messages.role],
    content = this[Messages.content],
    clientMessageId = this[Messages.clientMessageId],
    status = this[Messages.status],
    createdAt = ISO.format(this[Messages.createdAt])
)

fun ResultRow.toUserDto(): UserDto = UserDto(
    id = this[AuthUsers.id].value.toString(),
    email = this[AuthUsers.email],
    phone = this[AuthUsers.phone],
    firstName = this.getOrNull(UserProfiles.firstName),
    lastName = this.getOrNull(UserProfiles.lastName),
    fullName = this[UserProfiles.firstName].orEmpty() + " " + this[UserProfiles.lastName]
)

fun ResultRow.toAdminTripDto(): UserTripDto = UserTripDto(
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
    createdAt = ISO.format(this[UserTrips.createdAt]),
    updatedAt = ISO.format(this[UserTrips.updatedAt]),
    tripTitle = this.getOrNull(CatalogTrips.title),
    tripImageUrl = this.getOrNull(CatalogTrips.imageUrl),
    tripRoute = this.getOrNull(CatalogTrips.route),
    tripDurationDays = this.getOrNull(CatalogTrips.durationDays),
    userName = this[UserProfiles.firstName].orEmpty(),
)

fun ResultRow.toCatalogTripDto(): CatalogTripDto = CatalogTripDto(
    id = this[CatalogTrips.id].value,
    title = this[CatalogTrips.title],
    imageUrl = this[CatalogTrips.imageUrl],
    durationDays = this[CatalogTrips.durationDays],
    route = this[CatalogTrips.route],
    priceCurrency = this[CatalogTrips.priceCurrency],
    priceBase = this[CatalogTrips.priceBase]?.toDouble(),
    priceSingleSupplement = this[CatalogTrips.priceSingleSupplement]?.toDouble(),
    priceTaxes = this[CatalogTrips.priceTaxes]?.toDouble(),
    includes = this[CatalogTrips.includes],
    notIncluded = this[CatalogTrips.notIncluded],
    requirements = this[CatalogTrips.requirements],
    url = this[CatalogTrips.url],
    createdAt = ISO.format(this[CatalogTrips.createdAt]),
    updatedAt = ISO.format(this[CatalogTrips.updatedAt]),
)
