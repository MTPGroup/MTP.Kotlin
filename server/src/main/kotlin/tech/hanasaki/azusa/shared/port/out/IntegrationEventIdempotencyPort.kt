package tech.hanasaki.azusa.shared.port.out

import tech.hanasaki.azusa.shared.domain.event.IntegrationEvent

interface IntegrationEventIdempotencyPort {
    /**
     * 尝试获取处理锁。返回 true 表示可以处理；false 表示已处理或正在被其他消费者处理。
     */
    suspend fun tryAcquire(event: IntegrationEvent, handlerKey: String): Boolean

    /**
     * 标记事件被处理成功。
     */
    suspend fun markProcessed(event: IntegrationEvent, handlerKey: String)

    /**
     * 释放处理锁（通常在处理失败时调用）。
     */
    suspend fun release(event: IntegrationEvent, handlerKey: String)
}
