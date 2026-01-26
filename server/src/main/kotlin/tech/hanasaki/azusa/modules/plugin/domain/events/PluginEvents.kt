package tech.hanasaki.azusa.modules.plugin.domain.events

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.common.kernel.event.DomainEvent
import tech.hanasaki.azusa.common.kernel.model.PluginId
import tech.hanasaki.azusa.common.kernel.model.UserId
import java.util.*
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class PluginCreatedEvent(
    val pluginId: PluginId,
    val authorId: UserId,
    val name: String,
    @Contextual
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Clock.System.now(),
) : DomainEvent

@Serializable
data class PluginApprovedEvent(
    val pluginId: PluginId,
    val authorId: UserId,
    @Contextual
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Clock.System.now(),
) : DomainEvent

@Serializable
data class PluginRejectedEvent(
    val pluginId: PluginId,
    val authorId: UserId,
    @Contextual
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Clock.System.now(),
) : DomainEvent

@Serializable
data class PluginSubscribedEvent(
    val pluginId: PluginId,
    val userId: UserId,
    @Contextual
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Clock.System.now(),
) : DomainEvent

@Serializable
data class PluginUnsubscribedEvent(
    val pluginId: PluginId,
    val userId: UserId,
    @Contextual
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Clock.System.now(),
) : DomainEvent
