package tech.hanasaki.azusa.shared.port.`in`

import tech.hanasaki.azusa.shared.domain.event.IntegrationEvent

interface EventSubscriberPort {
    fun registerHandler(eventType: String, handler: suspend (IntegrationEvent) -> Unit)
}
