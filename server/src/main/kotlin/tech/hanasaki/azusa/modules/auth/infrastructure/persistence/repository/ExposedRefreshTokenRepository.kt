package tech.hanasaki.azusa.modules.auth.infrastructure.persistence.repository


import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import tech.hanasaki.azusa.modules.auth.domain.model.RefreshToken
import tech.hanasaki.azusa.modules.auth.domain.model.UserId
import tech.hanasaki.azusa.modules.auth.domain.repository.RefreshTokenRepository
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table.RefreshTokensTable
import tech.hanasaki.azusa.shared.infrastructure.database.dbQuery

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
        id = row[RefreshTokensTable.id].value,
        userId = UserId(row[RefreshTokensTable.userId].value),
        tokenHash = row[RefreshTokensTable.tokenHash],
        expiresAt = row[RefreshTokensTable.expiresAt],
        isRevoked = row[RefreshTokensTable.isRevoked]
    )
}