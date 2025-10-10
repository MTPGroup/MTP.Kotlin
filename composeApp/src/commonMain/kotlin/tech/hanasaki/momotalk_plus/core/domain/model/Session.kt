package tech.hanasaki.momotalk_plus.core.domain.model

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 会话领域模型
 */
data class Session(
    val id: String,
    val token: String,
    val expiresAt: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val ipAddress: String,
    val userAgent: String,
    val userId: String,
) {
    /**
     * 检查会话是否已过期
     */
    @OptIn(ExperimentalTime::class)
    fun isExpired(): Boolean {
        return expiresAt?.let {
            Clock.System.now() > Instant.parse(it)
        } ?: false
    }

    /**
     * 检查会话是否有效
     */
    fun isValid(): Boolean = !isExpired()
}

