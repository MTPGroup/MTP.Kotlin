package tech.hanasaki.azusa.modules.auth.adapter.`in`.event

import tech.hanasaki.azusa.common.port.`in`.EventHandler
import tech.hanasaki.azusa.modules.auth.application.service.OtpService
import tech.hanasaki.azusa.modules.auth.domain.event.UserRegistered
import tech.hanasaki.azusa.modules.auth.domain.model.OtpType


class UserRegisteredHandler(
    private val otpService: OtpService,
) : EventHandler<UserRegistered> {

    override suspend fun invoke(event: UserRegistered) {
        otpService.generateOtp(event.email, OtpType.VERIFY_EMAIL)
    }
}
