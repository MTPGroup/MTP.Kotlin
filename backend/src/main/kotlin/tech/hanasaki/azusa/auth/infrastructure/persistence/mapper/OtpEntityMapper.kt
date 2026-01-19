package tech.hanasaki.azusa.auth.infrastructure.persistence.mapper

import org.springframework.stereotype.Component
import tech.hanasaki.azusa.auth.domain.model.Email
import tech.hanasaki.azusa.auth.domain.model.Otp
import tech.hanasaki.azusa.auth.domain.model.OtpType
import tech.hanasaki.azusa.auth.infrastructure.persistence.entity.OtpEntity
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Component
class OtpEntityMapper {
    fun toEntity(domain: Otp, isNewRecord: Boolean = false): OtpEntity = OtpEntity(
        id = domain.id,
        email = domain.email.value,
        type = domain.type.value,
        codeHash = domain.codeHash,
        isUsed = domain.isUsed,
        expiresAt = domain.expiresAt.toJavaInstant(),
        createdAt = domain.createAt.toJavaInstant(),
        usedAt = domain.usedAt?.toJavaInstant(),
    ).apply {
        this.isNewRecord = isNewRecord
    }

    fun toDomain(entity: OtpEntity): Otp = Otp(
        id = entity.id,
        email = Email(entity.email),
        codeHash = entity.codeHash,
        type = OtpType.fromValue(entity.type),
        isUsed = entity.isUsed,
        createAt = entity.createdAt.toKotlinInstant(),
        expiresAt = entity.expiresAt.toKotlinInstant(),
        usedAt = entity.usedAt?.toKotlinInstant(),
    )
}