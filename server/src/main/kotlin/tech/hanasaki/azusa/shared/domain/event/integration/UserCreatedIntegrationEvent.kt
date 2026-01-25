package tech.hanasaki.azusa.shared.domain.event.integration

import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.shared.domain.event.DomainEvent
import tech.hanasaki.azusa.shared.domain.model.UserId
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * 用户创建集成事件 - 跨模块共享
 *
 * 当用户注册完成后发布，供其他模块订阅以初始化关联数据（如 Setting、Profile 等）
 */
@Serializable
data class UserCreatedIntegrationEvent(
    val userId: UserId,
    override val occurredAt: Instant = Clock.System.now(),
) : DomainEvent
