package tech.hanasaki.azusa.modules.auth.application.listener

import org.slf4j.LoggerFactory
import tech.hanasaki.azusa.common.kernel.event.EventListener
import tech.hanasaki.azusa.modules.auth.application.service.OtpService
import tech.hanasaki.azusa.modules.auth.domain.event.UserRegisteredEvent
import tech.hanasaki.azusa.modules.auth.domain.model.OtpType


class UserRegisteredListener(
    private val otpService: OtpService,
) : EventListener<UserRegisteredEvent> {
    private val logger = LoggerFactory.getLogger(UserRegisteredListener::class.java)

    override suspend fun handle(event: UserRegisteredEvent) {
        logger.info("User registered: ${event.userId}")
        otpService.generateOtp(event.email, OtpType.VERIFY_EMAIL)
        // TODO: 转换为集成事件
    }
}