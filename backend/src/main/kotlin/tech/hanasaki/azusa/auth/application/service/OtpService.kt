package tech.hanasaki.azusa.auth.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.hanasaki.azusa.auth.application.port.NotificationService
import tech.hanasaki.azusa.auth.application.port.PasswordEncoder
import tech.hanasaki.azusa.auth.domain.model.Email
import tech.hanasaki.azusa.auth.domain.model.Otp
import tech.hanasaki.azusa.auth.domain.model.OtpType
import tech.hanasaki.azusa.auth.domain.repository.OtpRepository
import tech.hanasaki.azusa.shared.AuthenticationException
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

@Service
class OtpService(
    private val otpRepository: OtpRepository,
    private val notificationService: NotificationService,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun sendOtp(email: Email, type: OtpType) {
        val code = generateCode()
        val otp = Otp(
            email = email,
            codeHash = passwordEncoder.encode(code),
            type = type,
            expiresAt = Clock.System.now().plus(3.minutes),
            createAt = Clock.System.now(),
        )
        otpRepository.save(otp)

        notificationService.sendOtp(email, code, type)
    }

    @Transactional
    fun verifyOtp(email: Email, type: OtpType, code: String) {
        val otp = otpRepository.findValidLatest(email, type)
            ?: throw AuthenticationException("Invalid or expired OTP")

        if (!passwordEncoder.matches(code, otp.codeHash)) {
            throw AuthenticationException("Invalid OTP code")
        }

        // 验证成功，标记为已使用
        otpRepository.markAsUsed(otp)
    }

    private fun generateCode(): String {
        return Random.nextInt(100000, 999999).toString()
    }
}
