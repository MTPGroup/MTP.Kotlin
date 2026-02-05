package tech.hanasaki.azusa.common.domain.event

/**
 * 集成事件监听器接口
 *
 * Application 层的事件处理器应实现此接口
 * 依赖应通过构造函数注入，invoke 方法只处理事件逻辑
 *
 * @param E 集成事件类型，必须实现 IntegrationEvent
 */
interface IntegrationEventListener<E : IntegrationEvent> {
    /**
     * 处理集成事件
     *
     * @param event 集成事件
     */
    suspend fun handle(event: E)
}
