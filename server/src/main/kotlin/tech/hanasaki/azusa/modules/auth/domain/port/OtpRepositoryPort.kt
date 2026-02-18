package tech.hanasaki.azusa.modules.auth.domain.port

import tech.hanasaki.azusa.shared.domain.model.vo.Email
import tech.hanasaki.azusa.modules.auth.domain.model.Otp
import tech.hanasaki.azusa.modules.auth.domain.model.OtpType
import kotlin.time.Instant

/**
 * OTP 仓储端口 - 被驱动端口（输出端口）
 */
interface OtpRepositoryPort {
    suspend fun save(otp: Otp)
    suspend fun findValidLatest(email: Email, type: OtpType): Otp?
    suspend fun markAsUsed(otp: Otp)

    /**
     * 统计指定时间之后发送的 OTP 数量（用于限流）
     */
    suspend fun countSentAfter(email: Email, type: OtpType, after: Instant): Int
}