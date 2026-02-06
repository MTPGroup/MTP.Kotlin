package tech.hanasaki.azusa.modules.auth.domain.model

import tech.hanasaki.azusa.shared.domain.exception.ValidationException
import tech.hanasaki.azusa.shared.domain.model.base.AggregateRoot
import tech.hanasaki.azusa.shared.domain.model.vo.Email
import tech.hanasaki.azusa.modules.auth.domain.event.OtpCreated
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

enum class OtpType {
    VERIFY_EMAIL,
    RESET_PASSWORD,
    SIGN_IN;

    companion object {
        fun fromString(value: String) = when (value.trim().lowercase()) {
            "verify_email" -> VERIFY_EMAIL
            "reset_password" -> RESET_PASSWORD
            "sign_in" -> SIGN_IN
            else -> throw ValidationException("不支持的验证码类型")
        }
    }
}


class Otp(
    val id: Uuid = Uuid.random(),
    val email: Email,
    val codeHash: String,
    val type: OtpType,
    val isUsed: Boolean = false,
    val expiresAt: Instant,
    val usedAt: Instant? = null,
) : AggregateRoot() {
    companion object {
        fun create(
            email: Email,
            codeHash: String,
            type: OtpType,
            expiresAt: Instant,
        ): Otp {
            val otp = Otp(
                email = email,
                codeHash = codeHash,
                type = type,
                expiresAt = expiresAt
            )
            otp.addDomainEvent(
                OtpCreated(
                    email = email,
                    otpType = type,
                    expiresAt = expiresAt
                )
            )
            return otp
        }
    }

    fun isValid(now: Instant = Clock.System.now()): Boolean = !isUsed && now <= expiresAt
}
