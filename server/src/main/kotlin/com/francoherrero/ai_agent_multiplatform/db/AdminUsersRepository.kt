package com.francoherrero.ai_agent_multiplatform.db

import com.francoherrero.ai_agent_multiplatform.model.UserDto
import com.francoherrero.ai_agent_multiplatform.model.UserRequest
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID

class AdminUsersRepository {

    private val joined
        get() = AuthUsers.join(UserProfiles, JoinType.LEFT, AuthUsers.id, UserProfiles.userId)

    private fun sortColumn(field: String?): Column<*> = when (field) {
        "email" -> AuthUsers.email
        "phone" -> AuthUsers.phone
        "first_name" -> UserProfiles.firstName
        "last_name" -> UserProfiles.lastName
        else -> AuthUsers.id
    }

    fun list(
        start: Long,
        end: Long,
        sortField: String?,
        sortOrder: String?,
    ): Pair<List<UserDto>, Long> = transaction {
        val total = AuthUsers.selectAll().count()
        val order = if (sortOrder?.uppercase() == "DESC") SortOrder.DESC else SortOrder.ASC
        val limit = (end - start).toInt()
        val offset = start

        val items = joined
            .selectAll()
            .orderBy(sortColumn(sortField), order)
            .limit(limit)
            .offset(offset)
            .map { it.toUserDto() }

        items to total
    }

    fun getById(id: UUID): UserDto? = transaction {
        joined
            .selectAll()
            .where { AuthUsers.id eq id }
            .map { it.toUserDto() }
            .singleOrNull()
    }

    fun update(id: UUID, request: UserRequest): UserDto? = transaction {
        // Verify user exists in auth.users
        val exists = AuthUsers.selectAll()
            .where { AuthUsers.id eq id }
            .singleOrNull() ?: return@transaction null

        // Upsert: check if profile exists
        val profileExists = UserProfiles.selectAll()
            .where { UserProfiles.userId eq id }
            .singleOrNull()

        if (profileExists != null) {
            UserProfiles.update({ UserProfiles.userId eq id }) {
                request.firstName?.let { v -> it[firstName] = v }
                request.lastName?.let { v -> it[lastName] = v }
            }
        } else {
            UserProfiles.insert {
                it[userId] = id
                it[firstName] = request.firstName
                it[lastName] = request.lastName
            }
        }

        joined
            .selectAll()
            .where { AuthUsers.id eq id }
            .single()
            .toUserDto()
    }

    fun delete(id: UUID): UserDto? = transaction {
        val dto = joined
            .selectAll()
            .where { AuthUsers.id eq id }
            .map { it.toUserDto() }
            .singleOrNull() ?: return@transaction null

        UserProfiles.deleteWhere { UserProfiles.userId eq id }
        dto
    }
}
