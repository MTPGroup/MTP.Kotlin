package tech.hanasaki.azusa.modules.auth.domain.model

import java.util.*
import kotlin.time.Clock
import kotlin.time.Instant

enum class OtpType(val value: String) {
    VERIFY_EMAIL("verify_email"),
    RESET_PASSWORD("reset_password"),
    SIGN_IN("sign_in");

    companion object {
        fun fromValue(value: String): OtpType = entries.firstOrNull { it.value == value || it.name == value }
            ?: throw IllegalArgumentException("Unknown OtpType value: $value")
    }
}

data class Otp(
    val id: UUID = UUID.randomUUID(),
    val email: Email,
    val codeHash: String,
    val type: OtpType,
    val isUsed: Boolean = false,
    val expiresAt: Instant,
    val usedAt: Instant? = null,
) {
    fun isValid(now: Instant = Clock.System.now()): Boolean = !isUsed && now <= expiresAt
}
