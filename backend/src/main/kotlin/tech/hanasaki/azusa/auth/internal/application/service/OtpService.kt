package tech.hanasaki.azusa.auth.internal.application.service

import org.springframework.stereotype.Service
import tech.hanasaki.azusa.auth.internal.domain.model.Email
import tech.hanasaki.azusa.auth.internal.domain.model.Otp
import tech.hanasaki.azusa.auth.internal.domain.model.OtpType
import tech.hanasaki.azusa.auth.internal.domain.repository.OtpRepository
import tech.hanasaki.azusa.shared.AuthenticationException
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

@Service
class OtpService(
    private val otpRepository: OtpRepository,
    private val emailService: EmailService,
) {
    suspend fun sendOtp(email: Email, type: OtpType) {
        val code = generateCode()
        val otp = Otp(
            email = email,
            codeHash = code,
            type = type,
            expiresAt = Clock.System.now().plus(3.minutes),
            createAt = Clock.System.now(),
        )
        otpRepository.save(otp)

        val subject = when (type) {
            OtpType.VERIFY_EMAIL -> "验证您的邮箱"
            OtpType.RESET_PASSWORD -> "重置您的密码"
            OtpType.SIGN_IN -> "登录验证"
        }

        val model = mapOf(
            "subject" to subject,
            "code" to code,
            "minutes" to 3
        )
        emailService.sendTemplate(email, subject, "email/otp.html", model)
    }

    suspend fun verifyOtp(email: Email, type: OtpType, code: String) {
        val otp = otpRepository.findValidLatest(email, type)
            ?: throw AuthenticationException("Invalid or expired OTP")

        if (otp.codeHash != code) {
            throw AuthenticationException("Invalid OTP code")
        }

        if (!otp.isExpired()) {
            throw AuthenticationException("OTP has expired")
        }

        // 验证成功，标记为已使用
        otpRepository.markAsUsed(otp)
    }

    private fun generateCode(): String {
        return Random.nextInt(100000, 999999).toString()
    }
}
