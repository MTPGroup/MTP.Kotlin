package tech.hanasaki.azusa.modules.auth.adapter.`in`.event

import tech.hanasaki.azusa.modules.auth.application.port.`in`.OtpUseCasePort
import tech.hanasaki.azusa.modules.auth.domain.event.UserRegistered
import tech.hanasaki.azusa.modules.auth.domain.model.OtpType
import tech.hanasaki.azusa.shared.port.`in`.DomainEventHandlerPort


class UserRegisteredHandler(
    private val otpService: OtpUseCasePort,
) : DomainEventHandlerPort<UserRegistered> {

    override suspend fun invoke(event: UserRegistered) {
        otpService.generate(event.email, OtpType.VERIFY_EMAIL)
    }
}
