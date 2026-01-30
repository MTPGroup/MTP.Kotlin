package tech.hanasaki.azusa.common.kernel.event

/**
 * 集成事件处理器
 *
 * 用于消费 IntegrationEvent 的处理接口
 *
 * @param E 集成事件类型
 */
fun interface IntegrationEventHandler<E : IntegrationEvent> {
    suspend fun handle(event: E)
}