package tech.hanasaki.azusa.shared.infrastructure.event

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import tech.hanasaki.azusa.shared.DomainEvent
import tech.hanasaki.azusa.shared.EventPublisher

/**
 * 基于 Spring ApplicationEventPublisher 的事件发布器
 * 支持 @TransactionalEventListener 实现事务感知的事件处理
 */
@Component
class SpringEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher,
) : EventPublisher {

    override suspend fun publish(event: DomainEvent) {
        // publishEvent 是同步的，但监听器可以是 @Async 的
        // 这里的 suspend 主要是为了兼容接口定义，Spring 内部是 Java API
        applicationEventPublisher.publishEvent(event)
    }

    override suspend fun publishAll(events: Collection<DomainEvent>) {
        events.forEach { applicationEventPublisher.publishEvent(it) }
    }
}