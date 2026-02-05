package tech.hanasaki.azusa.modules.auth.application.service

import tech.hanasaki.azusa.common.domain.exception.AuthenticationException
import tech.hanasaki.azusa.common.domain.exception.DomainException
import tech.hanasaki.azusa.common.domain.model.Email
import tech.hanasaki.azusa.common.port.out.OutboxScheduler
import tech.hanasaki.azusa.common.port.out.TransactionalPort
import tech.hanasaki.azusa.modules.auth.application.port.out.OtpRepository
import tech.hanasaki.azusa.modules.auth.config.OtpConfig
import tech.hanasaki.azusa.modules.auth.domain.model.Otp
import tech.hanasaki.azusa.modules.auth.domain.model.OtpType
import java.security.MessageDigest
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class OtpService(
    private val otpRepository: OtpRepository,
    private val otpConfig: OtpConfig,
    private val outboxScheduler: OutboxScheduler,
    private val tx: TransactionalPort,
) {
    companion object {
        private const val OTP_EXPIRE_MINUTES = 10
        private const val MAX_OTP_PER_HOUR = 5
    }

    suspend fun generateOtp(email: Email, type: OtpType) {
        tx.execute {
            val oneHourAgo = Clock.System.now().minus(1.hours)
            val sentCount = otpRepository.countSentAfter(email, type, oneHourAgo)
            if (sentCount >= MAX_OTP_PER_HOUR) {
                throw DomainException("OTP请求已达限制。请稍后再试。")
            }

            val code = if (otpConfig.testMode) otpConfig.testCode else generateCode()
            val otp = Otp.create(
                email = email,
                codeHash = hashCode(code),
                type = type,
                expiresAt = Clock.System.now().plus(OTP_EXPIRE_MINUTES.minutes)
            )
            otpRepository.save(otp)

            otp.domainEvents.forEach { event ->
                outboxScheduler.schedule(event)
            }

            otp.clearDomainEvents()
        }
    }

    suspend fun verifyOtp(email: Email, type: OtpType, code: String) {
        tx.execute {
            val otp = otpRepository.findValidLatest(email, type)
                ?: throw AuthenticationException("非法或已过期的验证码")

            if (!verifyHash(code, otp.codeHash)) {
                throw AuthenticationException("非法的验证码")
            }

            if (!otp.isValid()) {
                throw AuthenticationException("验证码已过期")
            }

            otpRepository.markAsUsed(otp)
        }
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