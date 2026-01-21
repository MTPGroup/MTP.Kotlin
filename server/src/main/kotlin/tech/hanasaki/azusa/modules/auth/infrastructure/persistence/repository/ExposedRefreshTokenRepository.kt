package tech.hanasaki.azusa.modules.auth.infrastructure.persistence.repository


import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import tech.hanasaki.azusa.modules.auth.domain.model.RefreshToken
import tech.hanasaki.azusa.modules.auth.domain.repository.RefreshTokenRepository
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table.RefreshTokensTable
import tech.hanasaki.azusa.shared.domain.model.UserId
import tech.hanasaki.azusa.shared.infrastructure.database.dbQuery
import kotlin.time.Clock

class ExposedRefreshTokenRepository : RefreshTokenRepository {

    override suspend fun save(refreshToken: RefreshToken): Unit = dbQuery {
        RefreshTokensTable.insert {
            it[id] = refreshToken.id
            it[userId] = refreshToken.userId.value
            it[tokenHash] = refreshToken.tokenHash
            it[expiresAt] = refreshToken.expiresAt
            it[createdAt] = Clock.System.now()
            it[isRevoked] = refreshToken.isRevoked
        }
    }

    override suspend fun findByTokenHash(tokenHash: String): RefreshToken? = dbQuery {
        RefreshTokensTable.selectAll()
            .where { RefreshTokensTable.tokenHash eq tokenHash }
            .map(::toRefreshToken)
            .singleOrNull()
    }

    override suspend fun revoke(refreshToken: RefreshToken): Unit = dbQuery {
        RefreshTokensTable.update({ RefreshTokensTable.id eq refreshToken.id }) {
            it[isRevoked] = true
        }
    }

    override suspend fun revokeAllForUser(userId: UserId): Unit = dbQuery {
        RefreshTokensTable.update({ RefreshTokensTable.userId eq userId.value }) {
            it[isRevoked] = true
        }
    }

    private fun toRefreshToken(row: ResultRow) = RefreshToken(
        id = row[RefreshTokensTable.id],
        userId = UserId(row[RefreshTokensTable.userId]),
        tokenHash = row[RefreshTokensTable.tokenHash],
        expiresAt = row[RefreshTokensTable.expiresAt],
        isRevoked = row[RefreshTokensTable.isRevoked]
    )
}
