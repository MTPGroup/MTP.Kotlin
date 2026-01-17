package tech.hanasaki.azusa.auth.application.service

import org.springframework.stereotype.Service
import tech.hanasaki.azusa.auth.domain.model.Email
import tech.hanasaki.azusa.auth.domain.model.Otp
import tech.hanasaki.azusa.auth.domain.model.OtpType
import tech.hanasaki.azusa.auth.domain.repository.OtpRepository
import tech.hanasaki.azusa.shared.domain.exception.AuthenticationException
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
            code = code,
            type = type,
            expiresAt = Clock.System.now().plus(3.minutes)
        )
        otpRepository.save(otp)

        val subject = when (type) {
            OtpType.VERIFY_EMAIL -> "验证您的邮箱"
            OtpType.RESET_PASSWORD -> "重置您的密码"
            OtpType.SIGN_IN -> "登录验证"
        }

        val html = """
            <h2>您的验证码为: <b>$code</b></h2>
            <p>此验证码将会在3分钟后过期.</p>
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
