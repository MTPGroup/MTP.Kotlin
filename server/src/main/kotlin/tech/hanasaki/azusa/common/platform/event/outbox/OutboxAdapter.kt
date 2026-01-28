package tech.hanasaki.azusa.common.platform.event.outbox

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import tech.hanasaki.azusa.common.kernel.event.IntegrationEvent
import tech.hanasaki.azusa.common.kernel.port.OutboxProvider
import tech.hanasaki.azusa.common.platform.event.outbox.model.OutboxEvent
import tech.hanasaki.azusa.common.platform.event.outbox.repository.OutboxEventRepository
import tech.hanasaki.azusa.common.platform.util.AppJson
import java.util.*
import kotlin.time.Clock

class OutboxAdapter(
    private val outboxEventRepository: OutboxEventRepository,
) : OutboxProvider {
    @OptIn(InternalSerializationApi::class)
    override suspend fun save(event: IntegrationEvent) {
        @Suppress("UNCHECKED_CAST")
        val serializer = event::class.serializer() as KSerializer<Any>
        val eventType = serializer.descriptor.serialName

        val outboxEvent = OutboxEvent(
            id = UUID.randomUUID(),
            eventType = eventType,
            payload = AppJson.json.encodeToJsonElement(serializer, event),
            createdAt = Clock.System.now(),
            sentAt = null,
        )
        outboxEventRepository.save(outboxEvent)
    }
}
