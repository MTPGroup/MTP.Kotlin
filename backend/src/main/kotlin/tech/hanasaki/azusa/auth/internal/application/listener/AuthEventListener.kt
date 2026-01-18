package tech.hanasaki.azusa.auth.internal.application.listener

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component
import tech.hanasaki.azusa.auth.UserRegisteredEvent
import tech.hanasaki.azusa.auth.internal.application.service.OtpService
import tech.hanasaki.azusa.auth.internal.domain.model.OtpType

@Component
class AuthEventListener(
    private val otpService: OtpService,
) {
    private val logger = LoggerFactory.getLogger(AuthEventListener::class.java)

    @ApplicationModuleListener
    fun onUserRegistered(event: UserRegisteredEvent) {
        runBlocking {
            logger.info("User registered event received for email: {}", event.email)
            try {
                otpService.sendOtp(event.email, OtpType.VERIFY_EMAIL)
                logger.info("OTP sent successfully to {}", event.email)
            } catch (e: Exception) {
                logger.error("Failed to send OTP for user ${event.email}", e)
            }
        }
    }
}