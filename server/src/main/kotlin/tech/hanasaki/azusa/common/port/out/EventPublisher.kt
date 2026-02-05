package tech.hanasaki.azusa.common.port.out

import tech.hanasaki.azusa.common.domain.event.DomainEvent

/**
 * 事件发布器接口
 */
interface EventPublisher {
    /**
     * 发布单个事件
     */
    suspend fun publish(event: DomainEvent)

    /**
     * 批量发布事件
     */
    suspend fun publishAll(events: Collection<DomainEvent>)
}


