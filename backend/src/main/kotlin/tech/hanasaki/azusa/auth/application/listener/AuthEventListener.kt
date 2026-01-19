package tech.hanasaki.azusa.auth.application.listener

import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component
import tech.hanasaki.azusa.auth.UserRegisteredEvent
import tech.hanasaki.azusa.auth.application.service.OtpService
import tech.hanasaki.azusa.auth.domain.model.OtpType

@Component
class AuthEventListener(
    private val otpService: OtpService,
    private val logger: Logger,
) {

    @ApplicationModuleListener
    fun onUserRegistered(event: UserRegisteredEvent) {
        runBlocking {
            try {
                otpService.sendOtp(event.email, OtpType.VERIFY_EMAIL)
            } catch (e: Exception) {
                logger.error("Failed to send OTP for user ${event.email}", e)
            }
        }
    }
}