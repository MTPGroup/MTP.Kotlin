package tech.hanasaki.azusa.modules.auth.application.service

import tech.hanasaki.azusa.modules.auth.OtpConfig
import tech.hanasaki.azusa.modules.auth.domain.events.OtpGeneratedEvent
import tech.hanasaki.azusa.modules.auth.domain.model.Email
import tech.hanasaki.azusa.modules.auth.domain.model.Otp
import tech.hanasaki.azusa.modules.auth.domain.model.OtpType
import tech.hanasaki.azusa.modules.auth.domain.repository.OtpRepository
import tech.hanasaki.azusa.shared.domain.event.EventPublisher
import tech.hanasaki.azusa.shared.domain.exception.AuthenticationException
import tech.hanasaki.azusa.shared.domain.exception.DomainException
import java.security.MessageDigest
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class OtpService(
    private val otpRepository: OtpRepository,
    private val eventPublisher: EventPublisher,
    private val otpConfig: OtpConfig = OtpConfig(),
) {
    companion object {
        private const val OTP_EXPIRE_MINUTES = 10
        private const val MAX_OTP_PER_HOUR = 5
    }

    suspend fun sendOtp(email: Email, type: OtpType) {
        // 限流检查：每小时最多发送 5 次
        val oneHourAgo = Clock.System.now().minus(1.hours)
        val sentCount = otpRepository.countSentAfter(email, type, oneHourAgo)
        if (sentCount >= MAX_OTP_PER_HOUR) {
            throw DomainException("OTP requests exceeded limit. Please try again later.")
        }

        // 测试模式使用固定验证码
        val code = if (otpConfig.testMode) otpConfig.testCode else generateCode()
        val otp = Otp(
            email = email,
            codeHash = hashCode(code),
            type = type,
            expiresAt = Clock.System.now().plus(OTP_EXPIRE_MINUTES.minutes)
        )
        otpRepository.save(otp)

        // 发布 OTP 生成事件，由通知模块异步处理
        eventPublisher.publish(
            OtpGeneratedEvent(
                email = email,
                code = code,
                otpType = type,
                expiresInMinutes = OTP_EXPIRE_MINUTES,
            )
        )
    }

    suspend fun verifyOtp(email: Email, type: OtpType, code: String) {
        val otp = otpRepository.findValidLatest(email, type)
            ?: throw AuthenticationException("Invalid or expired OTP")

        if (!verifyHash(code, otp.codeHash)) {
            throw AuthenticationException("Invalid OTP code")
        }

        if (!otp.isValid()) {
            throw AuthenticationException("OTP has expired")
        }

        otpRepository.markAsUsed(otp)
    }

    private fun generateCode(): String {
        return Random.nextInt(100000, 999999).toString()
    }

    private fun hashCode(code: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(code.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private fun verifyHash(code: String, storedHash: String): Boolean {
        return hashCode(code) == storedHash
    }
}
