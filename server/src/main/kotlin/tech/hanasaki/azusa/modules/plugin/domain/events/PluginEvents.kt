package tech.hanasaki.azusa.modules.plugin.domain.events

import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.shared.domain.event.DomainEvent
import tech.hanasaki.azusa.shared.domain.model.vo.PluginId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid


@Serializable
sealed class PluginEvent : DomainEvent {
    abstract val pluginId: PluginId
    override val aggregateId: String get() = pluginId.toString()
    override val aggregateType: String get() = "Plugin"
    override val occurredOn: Instant get() = Clock.System.now()
}

@Serializable
data class PluginCreated(
    override val pluginId: PluginId,
    val authorId: UserId,
    val name: String,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "plugin.created",
) : PluginEvent()


@Serializable
data class PluginApproved(
    override val pluginId: PluginId,
    val authorId: UserId,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "plugin.approved",
) : PluginEvent()

@Serializable
data class PluginRejected(
    override val pluginId: PluginId,
    val authorId: UserId,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "plugin.rejected",
) : PluginEvent()


