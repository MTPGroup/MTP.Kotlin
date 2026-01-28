package tech.hanasaki.azusa.modules.auth.infrastructure.persistence.repository

import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import tech.hanasaki.azusa.common.kernel.model.Email
import tech.hanasaki.azusa.common.kernel.model.UserId
import tech.hanasaki.azusa.common.platform.database.dbQuery
import tech.hanasaki.azusa.modules.auth.domain.model.User
import tech.hanasaki.azusa.modules.auth.domain.repository.UserRepository
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.mapper.ProfileMapper
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.mapper.UserMapper
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table.ProfileTable
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table.UserTable
import kotlin.time.Clock

class ExposedUserRepository : UserRepository {
    override suspend fun findByEmail(email: Email): User? = dbQuery {
        UserTable.join(
            ProfileTable,
            JoinType.INNER,
            UserTable.id,
            ProfileTable.uid
        )
            .selectAll()
            .where { UserTable.email eq email.value }
            .map(UserMapper::toDomain)
            .singleOrNull()
    }

    override suspend fun findById(id: UserId): User? = dbQuery {
        UserTable.join(
            ProfileTable,
            JoinType.INNER,
            UserTable.id,
            ProfileTable.uid
        )
            .selectAll()
            .where { UserTable.id eq id.value }
            .map(UserMapper::toDomain)
            .singleOrNull()
    }

    override suspend fun save(user: User): Unit = dbQuery {
        val userRowsUpdated = UserTable.update({ UserTable.id eq user.id.value }) {
            UserMapper.toEntity(user, it)
            it[updatedAt] = Clock.System.now()
        }

        if (userRowsUpdated == 0) {
            UserTable.insert {
                UserMapper.toEntity(user, it)
            }
        }

        val profileRowsUpdated = ProfileTable.update({ ProfileTable.uid eq user.id.value }) {
            ProfileMapper.toEntity(user.profile, it)
            it[updatedAt] = Clock.System.now()
        }

        if (profileRowsUpdated == 0) {
            ProfileTable.insert {
                ProfileMapper.toEntity(user.profile, it)
                it[updatedAt] = user.profile.updatedAt
            }
        }
    }

    override suspend fun deleteById(id: UserId): Unit = dbQuery {
        UserTable.deleteWhere { UserTable.id eq id.value }
    }
}
