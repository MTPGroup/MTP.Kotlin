package tech.hanasaki.azusa.auth.internal.infrastructure.persistence.mapper

import org.springframework.stereotype.Component
import tech.hanasaki.azusa.auth.internal.domain.model.Email
import tech.hanasaki.azusa.auth.internal.domain.model.Otp
import tech.hanasaki.azusa.auth.internal.domain.model.OtpType
import tech.hanasaki.azusa.auth.internal.infrastructure.persistence.entity.OtpEntity
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Component
class OtpEntityMapper {
    fun toEntity(otp: Otp): OtpEntity = OtpEntity(
        id = otp.id,
        email = otp.email.value,
        type = otp.type.value,
        codeHash = otp.codeHash,
        isUsed = otp.isUsed,
        expiresAt = otp.expiresAt.toJavaInstant(),
        createdAt = otp.createAt.toJavaInstant(),
        usedAt = otp.usedAt?.toJavaInstant()
    )

    fun toDomain(otpEntity: OtpEntity): Otp = Otp(
        id = otpEntity.id,
        email = Email(otpEntity.email),
        codeHash = otpEntity.codeHash,
        type = OtpType.valueOf(otpEntity.type),
        isUsed = otpEntity.isUsed,
        createAt = otpEntity.createdAt.toKotlinInstant(),
        expiresAt = otpEntity.expiresAt.toKotlinInstant(),
        usedAt = otpEntity.usedAt?.toKotlinInstant(),
    )
}