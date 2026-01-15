package tech.hanasaki.azusa.modules.auth.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.*

enum class OtpType(val value: String) {
    VERIFY_EMAIL("verify_email"),
    RESET_PASSWORD("reset_password"),
    SIGN_IN("sign_in")
}

data class Otp(
    val id: UUID = UUID.randomUUID(),
    val email: Email,
    val code: String,
    val type: OtpType,
    val expiresAt: Instant,
    val isUsed: Boolean = false,
) {
    fun isValid(now: Instant = Clock.System.now()): Boolean = !isUsed && now <= expiresAt
}