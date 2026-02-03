package tech.hanasaki.azusa.modules.auth.domain.model

import tech.hanasaki.azusa.common.kernel.base.AggregateRoot
import tech.hanasaki.azusa.common.kernel.model.Email
import tech.hanasaki.azusa.modules.auth.domain.event.OtpGeneratedEvent
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

enum class OtpType(val value: String) {
    VERIFY_EMAIL("verify_email"),
    RESET_PASSWORD("reset_password"),
    SIGN_IN("sign_in");

    companion object {
        fun fromValue(value: String): OtpType = entries.firstOrNull { it.value == value || it.name == value }
            ?: throw IllegalArgumentException("Unknown OtpType value: $value")
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
            code: String,
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
                OtpGeneratedEvent(
                    email = email,
                    code = code,
                    otpType = type,
                    expiresAt = expiresAt
                )
            )
            return otp
        }
    }

    fun isValid(now: Instant = Clock.System.now()): Boolean = !isUsed && now <= expiresAt
}
