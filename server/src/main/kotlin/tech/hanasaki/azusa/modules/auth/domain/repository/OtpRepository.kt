package tech.hanasaki.azusa.modules.auth.domain.repository

import tech.hanasaki.azusa.modules.auth.domain.model.Email
import tech.hanasaki.azusa.modules.auth.domain.model.Otp
import tech.hanasaki.azusa.modules.auth.domain.model.OtpType
import kotlin.time.Instant

interface OtpRepository {
    suspend fun save(otp: Otp)
    suspend fun findValidLatest(email: Email, type: OtpType): Otp?
    suspend fun markAsUsed(otp: Otp)

    /**
     * 统计指定时间之后发送的 OTP 数量（用于限流）
     */
    suspend fun countSentAfter(email: Email, type: OtpType, after: Instant): Int
}