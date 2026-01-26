package tech.hanasaki.azusa.auth.infrastructure.persistence.repository

import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.stereotype.Repository
import tech.hanasaki.azusa.auth.domain.model.RefreshToken
import tech.hanasaki.azusa.auth.domain.repository.RefreshTokenRepository
import tech.hanasaki.azusa.auth.infrastructure.persistence.mapper.RefreshTokenEntityMapper
import tech.hanasaki.azusa.common.UserId

@Repository
class JdbcRefreshTokenRepository(
    private val aggregateTemplate: JdbcAggregateTemplate,
    private val refreshTokenRepository: SpringDataRefreshTokenEntityRepository,
    private val mapper: RefreshTokenEntityMapper,
) : RefreshTokenRepository {
    override fun save(refreshToken: RefreshToken) {
        val exists = refreshTokenRepository.existsById(refreshToken.id)
        val entity = mapper.toEntity(refreshToken, !exists)
        aggregateTemplate.save(entity)
    }

    override fun findByTokenHash(tokenHash: String): RefreshToken? =
        refreshTokenRepository.findByTokenHash(tokenHash)?.let(mapper::toDomain)

    override fun revoke(refreshToken: RefreshToken) {
        val entity = mapper.toEntity(refreshToken.copy(isRevoked = true))
        aggregateTemplate.save(entity)
    }


    override fun revokeAllForUser(userId: UserId) {
        refreshTokenRepository.findAllByUserId(userId.value).forEach { entity ->
            aggregateTemplate.save(entity.copy(isRevoked = true))
        }
    }
}
