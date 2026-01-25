package tech.hanasaki.azusa.modules.auth.infrastructure.persistence.repository


import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import tech.hanasaki.azusa.modules.auth.domain.model.RefreshToken
import tech.hanasaki.azusa.modules.auth.domain.repository.RefreshTokenRepository
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.mapper.RefreshTokenMapper
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table.RefreshTokensTable
import tech.hanasaki.azusa.shared.domain.model.UserId
import tech.hanasaki.azusa.shared.infrastructure.database.dbQuery

class ExposedRefreshTokenRepository : RefreshTokenRepository {

    override suspend fun save(refreshToken: RefreshToken): Unit = dbQuery {
        val updatedToken = RefreshTokensTable.update({ RefreshTokensTable.id eq refreshToken.id }) {
            RefreshTokenMapper.toEntity(refreshToken, it)
        }
        if (updatedToken == 0) {
            RefreshTokensTable.insert {
                RefreshTokenMapper.toEntity(refreshToken, it)
            }
        }
    }

    override suspend fun findByTokenHash(tokenHash: String): RefreshToken? = dbQuery {
        RefreshTokensTable.selectAll()
            .where { RefreshTokensTable.tokenHash eq tokenHash }
            .map(RefreshTokenMapper::toDomain)
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
}
