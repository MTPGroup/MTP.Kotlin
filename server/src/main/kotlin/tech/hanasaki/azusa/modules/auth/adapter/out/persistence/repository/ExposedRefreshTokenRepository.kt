package tech.hanasaki.azusa.modules.auth.adapter.out.persistence.repository


import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import tech.hanasaki.azusa.modules.auth.adapter.out.persistence.mapper.RefreshTokenMapper
import tech.hanasaki.azusa.modules.auth.adapter.out.persistence.table.RefreshTokenTable
import tech.hanasaki.azusa.modules.auth.domain.model.RefreshToken
import tech.hanasaki.azusa.modules.auth.domain.port.RefreshTokenRepositoryPort
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

class ExposedRefreshTokenRepository : RefreshTokenRepositoryPort {

    override suspend fun save(refreshToken: RefreshToken) {
        val updatedToken = RefreshTokenTable.update({ RefreshTokenTable.id eq refreshToken.id }) {
            RefreshTokenMapper.toEntity(refreshToken, it)
        }
        if (updatedToken == 0) {
            RefreshTokenTable.insert {
                RefreshTokenMapper.toEntity(refreshToken, it)
            }
        }
    }

    override suspend fun findByTokenHash(tokenHash: String): RefreshToken? {
        return RefreshTokenTable.selectAll()
            .where { RefreshTokenTable.tokenHash eq tokenHash }
            .map(RefreshTokenMapper::toDomain)
            .singleOrNull()
    }

    override suspend fun revoke(refreshToken: RefreshToken) {
        RefreshTokenTable.update({ RefreshTokenTable.id eq refreshToken.id }) {
            it[isRevoked] = true
        }
    }

    override suspend fun revokeAllForUser(userId: UserId) {
        RefreshTokenTable.update({ RefreshTokenTable.userId eq userId.value }) {
            it[isRevoked] = true
        }
    }
}
