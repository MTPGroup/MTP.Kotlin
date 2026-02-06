package tech.hanasaki.azusa.shared.port.out

import tech.hanasaki.azusa.shared.domain.event.IntegrationEvent

interface OutboxSchedulerPort {
    suspend fun schedule(event: IntegrationEvent)
}
