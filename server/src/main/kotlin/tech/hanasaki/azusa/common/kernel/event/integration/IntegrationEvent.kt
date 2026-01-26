package tech.hanasaki.azusa.common.kernel.event.integration

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.common.kernel.event.DomainEvent
import java.util.*
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * 集成事件 - 用于跨模块通信
 *
 * 所有需要跨模块发布的事件都应继承此接口。
 * 与 DomainEvent 的区别：
 * - DomainEvent: 领域内部事件，仅在聚合根内部或模块内使用
 * - IntegrationEvent: 跨模块事件，用于模块间解耦通信
 */
@Serializable
abstract class IntegrationEvent : DomainEvent {
    @Contextual
    override val eventId: UUID = UUID.randomUUID()
    override val occurredAt: Instant = Clock.System.now()
}

/**
 * 用户资源初始化事件
 *
 * 当新用户注册时发布，用于其他模块初始化用户相关的资源。
 */
@Serializable
data class UserResourcesInitEvent(
    val userId: String,
) : IntegrationEvent()
