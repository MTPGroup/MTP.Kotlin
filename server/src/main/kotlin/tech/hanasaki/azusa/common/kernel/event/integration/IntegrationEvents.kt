package tech.hanasaki.azusa.common.kernel.event.integration

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.common.kernel.event.DomainEvent
import java.util.*
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * 用户资源初始化事件
 *
 * 当新用户注册时发布，用于其他模块初始化用户相关的资源（如 Setting、Character、Theme 等）。
 * 各模块通过订阅此事件来执行相应的初始化操作，实现模块间的低耦合通信。
 */
@Serializable
data class InitializeUserResources(
    val userId: String,
    @Contextual
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Clock.System.now(),
) : DomainEvent
