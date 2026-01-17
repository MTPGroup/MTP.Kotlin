package tech.hanasaki.azusa.auth.internal.infrastructure.persistence.mapper

import org.springframework.stereotype.Component
import tech.hanasaki.azusa.auth.internal.domain.model.RefreshToken
import tech.hanasaki.azusa.auth.internal.domain.model.UserId
import tech.hanasaki.azusa.auth.internal.infrastructure.persistence.entity.RefreshTokenEntity
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Component
class RefreshTokenEntityMapper {
    fun toEntity(refreshToken: RefreshToken): RefreshTokenEntity = RefreshTokenEntity(
        id = refreshToken.id,
        userId = refreshToken.userId.value,
        tokenHash = refreshToken.tokenHash,
        expiresAt = refreshToken.expiresAt.toJavaInstant(),
        createdAt = refreshToken.createdAt.toJavaInstant(),
        isRevoked = refreshToken.isRevoked
    )

    fun toDomain(refreshTokenEntity: RefreshTokenEntity): RefreshToken = RefreshToken(
        id = refreshTokenEntity.id,
        userId = UserId(refreshTokenEntity.userId),
        tokenHash = refreshTokenEntity.tokenHash,
        expiresAt = refreshTokenEntity.expiresAt.toKotlinInstant(),
        createdAt = refreshTokenEntity.createdAt.toKotlinInstant(),
        isRevoked = refreshTokenEntity.isRevoked
    )
}