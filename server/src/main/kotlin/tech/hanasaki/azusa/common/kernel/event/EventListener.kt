package tech.hanasaki.azusa.common.kernel.event

/**
 * 事件监听器接口
 */
fun interface EventListener<T : DomainEvent> {
    suspend fun handle(event: T)
}
