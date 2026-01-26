package tech.hanasaki.azusa.common.kernel.event

/**
 * 事件订阅器接口
 */
enum class SubscriptionMode {
    /**
     * 同步模式：监听器在发布者的协程上下文中执行
     * 适用于需要参与发布者事务的场景
     * 注意：如果监听器抛出异常，会向上传播并可能导致事务回滚
     */
    SYNCHRONOUS,

    /**
     * 异步模式：监听器在独立的协程中执行
     * 适用于不需要强一致性、允许最终一致性的场景
     * 异常会被捕获并记录日志，不会影响发布者
     */
    ASYNCHRONOUS
}

interface EventSubscriber {
    /**
     * 订阅特定类型的事件
     * @param mode 订阅模式，确监听器是同步执行还是异步执行
     */
    suspend fun <T : DomainEvent> subscribe(
        eventType: Class<T>,
        listener: EventListener<T>,
        mode: SubscriptionMode = SubscriptionMode.ASYNCHRONOUS
    )

    suspend fun <T : DomainEvent> unsubscribe(eventType: Class<T>, listener: EventListener<T>)
}
