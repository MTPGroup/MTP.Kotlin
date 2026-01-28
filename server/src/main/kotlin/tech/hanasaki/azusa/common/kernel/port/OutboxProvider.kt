package tech.hanasaki.azusa.common.kernel.port

import tech.hanasaki.azusa.common.kernel.event.IntegrationEvent

interface OutboxProvider {
    /**
     * 将集成事件保存到 OutBox
     */
    suspend fun save(event: IntegrationEvent)
}