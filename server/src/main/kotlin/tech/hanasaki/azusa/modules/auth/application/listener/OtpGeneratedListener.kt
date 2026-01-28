package tech.hanasaki.azusa.modules.auth.application.listener

import org.slf4j.LoggerFactory
import tech.hanasaki.azusa.common.kernel.event.EventListener
import tech.hanasaki.azusa.common.kernel.event.OtpGeneratedIntegrationEvent
import tech.hanasaki.azusa.common.kernel.port.OutboxProvider
import tech.hanasaki.azusa.modules.auth.domain.event.OtpGeneratedEvent

class OtpGeneratedListener(
    private val outboxAdapter: OutboxProvider,
) : EventListener<OtpGeneratedEvent> {
    private val logger = LoggerFactory.getLogger(OtpGeneratedListener::class.java)

    override suspend fun handle(event: OtpGeneratedEvent) {
        val integrationEvent = OtpGeneratedIntegrationEvent(
            email = event.email.value,
            code = event.code,
            type = event.otpType.name
        )
        outboxAdapter.save(integrationEvent)
        logger.info("${event.eventId}: ${integrationEvent.type}")
    }
}