package tech.hanasaki.azusa.auth.internal.infrastructure.persistence.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    override suspend fun save(refreshToken: RefreshToken): Unit = withContext(Dispatchers.IO) {
        val entity = mapper.toEntity(refreshToken)
        if (refreshTokenRepository.existsById(refreshToken.id)) {
            aggregateTemplate.save(entity)
        } else {
            aggregateTemplate.insert(entity)
        }
    }

    override suspend fun findByTokenHash(tokenHash: String): RefreshToken? = withContext(Dispatchers.IO) {
        refreshTokenRepository.findByTokenHash(tokenHash)?.let(mapper::toDomain)
    }

    override suspend fun revoke(refreshToken: RefreshToken): Unit = withContext(Dispatchers.IO) {
        val entity = mapper.toEntity(refreshToken.copy(isRevoked = true))
        aggregateTemplate.save(entity)
    }


    override suspend fun revokeAllForUser(userId: UserId): Unit = withContext(Dispatchers.IO) {
        refreshTokenRepository.findAllByUserId(userId.value).forEach { entity ->
            aggregateTemplate.save(entity.copy(isRevoked = true))
        }
    }
}
