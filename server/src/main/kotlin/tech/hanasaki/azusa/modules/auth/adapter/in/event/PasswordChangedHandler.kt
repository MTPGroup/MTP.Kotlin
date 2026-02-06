package tech.hanasaki.azusa.modules.auth.adapter.`in`.event

import tech.hanasaki.azusa.modules.auth.application.port.`in`.AuthUseCasePort
import tech.hanasaki.azusa.modules.auth.domain.event.PasswordChanged
import tech.hanasaki.azusa.shared.port.`in`.DomainEventHandlerPort

class PasswordChangedHandler(
    private val authUseCase: AuthUseCasePort,
) : DomainEventHandlerPort<PasswordChanged> {
    override suspend fun invoke(event: PasswordChanged) {
        authUseCase.onPasswordChanged(event.userId, event.email?.value)
    }
}
