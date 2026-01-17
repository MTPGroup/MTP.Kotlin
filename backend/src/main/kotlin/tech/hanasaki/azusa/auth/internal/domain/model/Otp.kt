package tech.hanasaki.azusa.auth.internal.domain.model

import java.util.*
import kotlin.time.Clock
import kotlin.time.Instant

enum class OtpType(val value: String) {
    VERIFY_EMAIL("verify_email"),
    RESET_PASSWORD("reset_password"),
    SIGN_IN("sign_in")
}

data class Otp(
    val id: UUID = UUID.randomUUID(),
    val email: Email,
    val codeHash: String,
    val type: OtpType,
    val createAt: Instant,
    val expiresAt: Instant,
    val usedAt: Instant? = null,
    val isUsed: Boolean = false,
) {
    fun isExpired(now: Instant = Clock.System.now()): Boolean = !isUsed && now <= expiresAt
}
