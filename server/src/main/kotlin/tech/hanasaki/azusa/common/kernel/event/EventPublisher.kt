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

/**
 * 事件监听器接口
 */
fun interface EventListener<T : DomainEvent> {
    suspend fun handle(event: T)
}

/**
 * 事件订阅器接口
 */
interface EventSubscriber {
    /**
     * 订阅特定类型的事件
     */
    fun <T : DomainEvent> subscribe(eventType: Class<T>, listener: EventListener<T>)
}
