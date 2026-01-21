package tech.hanasaki.azusa.modules.auth.application.service

import tech.hanasaki.azusa.modules.auth.application.port.EmailService
import tech.hanasaki.azusa.modules.auth.domain.model.Email
import tech.hanasaki.azusa.modules.auth.domain.model.Otp
import tech.hanasaki.azusa.modules.auth.domain.model.OtpType
import tech.hanasaki.azusa.modules.auth.domain.repository.OtpRepository
import tech.hanasaki.azusa.shared.domain.exception.AuthenticationException
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

class OtpService(
    private val otpRepository: OtpRepository,
    private val emailService: EmailService,
) {
    suspend fun sendOtp(email: Email, type: OtpType) {
        val code = generateCode()
        val otp = Otp(
            email = email,
            code = code,
            type = type,
            expiresAt = Clock.System.now().plus(10.minutes) // 10分钟有效期
        )
        otpRepository.save(otp)

        val subject = when (type) {
            OtpType.VERIFY_EMAIL -> "Verify your email"
            OtpType.RESET_PASSWORD -> "Reset your password"
            OtpType.SIGN_IN -> "Sign in code"
        }

        val html = """
            <h2>Your verification code is: <b>$code</b></h2>
            <p>This code will expire in 10 minutes.</p>
        """.trimIndent()

        emailService.sendHtml(email, subject, html)
    }

    suspend fun verifyOtp(email: Email, type: OtpType, code: String) {
        val otp = otpRepository.findValidLatest(email, type)
            ?: throw AuthenticationException("Invalid or expired OTP")

        if (otp.code != code) {
            throw AuthenticationException("Invalid OTP code")
        }

        if (!otp.isValid()) {
            throw AuthenticationException("OTP has expired")
        }

        // 验证成功，标记为已使用
        otpRepository.markAsUsed(otp)
    }

    private fun generateCode(): String {
        return Random.nextInt(100000, 999999).toString()
    }
}
