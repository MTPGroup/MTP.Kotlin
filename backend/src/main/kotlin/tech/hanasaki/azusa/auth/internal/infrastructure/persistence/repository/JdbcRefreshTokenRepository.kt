package tech.hanasaki.azusa.auth.internal.infrastructure.persistence.repository

import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.stereotype.Repository
import tech.hanasaki.azusa.auth.internal.domain.model.RefreshToken
import tech.hanasaki.azusa.auth.internal.domain.model.UserId
import tech.hanasaki.azusa.auth.internal.domain.repository.RefreshTokenRepository
import tech.hanasaki.azusa.auth.internal.infrastructure.persistence.mapper.RefreshTokenEntityMapper

@Repository
class JdbcRefreshTokenRepository(
    private val aggregateTemplate: JdbcAggregateTemplate,
    private val refreshTokenRepository: SpringDataRefreshTokenEntityRepository,
    private val mapper: RefreshTokenEntityMapper,
) : RefreshTokenRepository {
    override fun save(refreshToken: RefreshToken) {
        val entity = mapper.toEntity(refreshToken)
        if (refreshTokenRepository.existsById(refreshToken.id)) {
            aggregateTemplate.save(entity)
        } else {
            aggregateTemplate.insert(entity)
        }
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
