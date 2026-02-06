package tech.hanasaki.azusa.modules.notification.adapter.`in`.event

import tech.hanasaki.azusa.modules.notification.application.port.`in`.NotificationUseCasePort
import tech.hanasaki.azusa.shared.domain.event.PasswordChangedIntegrationEvent
import tech.hanasaki.azusa.shared.port.`in`.IntegrationEventHandlerPort

class PasswordChangedHandler(
    private val notificationService: NotificationUseCasePort,
) : IntegrationEventHandlerPort<PasswordChangedIntegrationEvent> {
    override suspend fun invoke(event: PasswordChangedIntegrationEvent) {
        if (event.email != null) {
            notificationService.sendEmail(
                to = event.email,
                subject = "密码已更改 - Azusa",
                templateName = "password-changed.ftl",
                model = emptyMap(),
            )
        }
    }

}