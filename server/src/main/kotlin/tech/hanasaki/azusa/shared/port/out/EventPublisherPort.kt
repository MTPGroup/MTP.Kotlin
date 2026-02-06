package tech.hanasaki.azusa.shared.port.out

import tech.hanasaki.azusa.shared.domain.event.DomainEvent

/**
 * 事件发布器接口
 */
interface EventPublisherPort {
    /**
     * 发布单个事件
     */
    suspend fun publish(event: DomainEvent)

    /**
     * 批量发布事件
     */
    suspend fun publishAll(events: Collection<DomainEvent>)
}


