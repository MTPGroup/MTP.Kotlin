package tech.hanasaki.azusa.common.kernel.event

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


