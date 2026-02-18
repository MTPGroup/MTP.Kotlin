package tech.hanasaki.azusa.modules.auth.adapter.`in`.event

import tech.hanasaki.azusa.modules.auth.application.port.`in`.OtpUseCasePort
import tech.hanasaki.azusa.modules.auth.domain.event.UserRegistered
import tech.hanasaki.azusa.modules.auth.domain.model.OtpType
import tech.hanasaki.azusa.shared.domain.event.UserRegisteredIntegrationEvent
import tech.hanasaki.azusa.shared.port.`in`.DomainEventHandlerPort
import tech.hanasaki.azusa.shared.port.out.OutboxSchedulerPort


class UserRegisteredHandler(
    private val otpService: OtpUseCasePort,
    private val outboxScheduler: OutboxSchedulerPort,
) : DomainEventHandlerPort<UserRegistered> {

    override suspend fun invoke(event: UserRegistered) {
        otpService.generate(event.email, OtpType.VERIFY_EMAIL)
        outboxScheduler.schedule(
            UserRegisteredIntegrationEvent(
                userId = event.userId.value
            )
        )
    }
}
