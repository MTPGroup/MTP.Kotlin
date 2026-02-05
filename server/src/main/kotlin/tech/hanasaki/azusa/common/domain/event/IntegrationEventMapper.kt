package tech.hanasaki.azusa.common.domain.event

/**
 * 领域事件到集成事件的映射器
 *
 * 负责将领域事件转换为跨模块通信的集成事件。
 * 这是事件驱动架构中的关键边界，确保领域模型与外部通信解耦。
 *
 * @param D 领域事件类型
 * @param I 集成事件类型
 */
interface IntegrationEventMapper<D : DomainEvent, I : IntegrationEvent> {
    /**
     * 将领域事件映射为集成事件
     *
     * @param domainEvent 领域事件
     * @return 对应的集成事件
     */
    fun map(domainEvent: D): I
}

/**
 * 支持异步数据获取的映射器
 *
 * 当映射过程需要查询数据库或其他外部资源时使用
 */
interface SuspendIntegrationEventMapper<D : DomainEvent, I : IntegrationEvent> {
    /**
     * 将领域事件映射为集成事件（挂起函数版本）
     *
     * @param domainEvent 领域事件
     * @return 对应的集成事件
     */
    suspend fun map(domainEvent: D): I
}
