package tech.hanasaki.azusa.auth.infrastructure.persistence.mapper

import org.springframework.stereotype.Component
import tech.hanasaki.azusa.auth.domain.model.RefreshToken
import tech.hanasaki.azusa.auth.infrastructure.persistence.entity.RefreshTokenEntity
import tech.hanasaki.azusa.shared.UserId
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Component
class RefreshTokenEntityMapper {
    fun toEntity(domain: RefreshToken, isNewRecord: Boolean = false): RefreshTokenEntity = RefreshTokenEntity(
        id = domain.id,
        userId = domain.userId.value,
        tokenHash = domain.tokenHash,
        expiresAt = domain.expiresAt.toJavaInstant(),
        createdAt = domain.createdAt.toJavaInstant(),
        isRevoked = domain.isRevoked,
    ).apply {
        this.isNewRecord = isNewRecord
    }

    fun toDomain(entity: RefreshTokenEntity): RefreshToken = RefreshToken(
        id = entity.id,
        userId = UserId(entity.userId),
        tokenHash = entity.tokenHash,
        expiresAt = entity.expiresAt.toKotlinInstant(),
        createdAt = entity.createdAt.toKotlinInstant(),
        isRevoked = entity.isRevoked
    )
}