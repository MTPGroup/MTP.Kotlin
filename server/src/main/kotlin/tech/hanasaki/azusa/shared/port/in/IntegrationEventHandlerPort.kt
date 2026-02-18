package tech.hanasaki.azusa.shared.port.`in`

import tech.hanasaki.azusa.shared.domain.event.IntegrationEvent

interface IntegrationEventHandlerPort<T : IntegrationEvent> {
    suspend operator fun invoke(event: T)
}
