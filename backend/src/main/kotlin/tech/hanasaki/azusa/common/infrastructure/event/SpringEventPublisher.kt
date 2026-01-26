package tech.hanasaki.azusa.common.infrastructure.event

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import tech.hanasaki.azusa.common.DomainEvent
import tech.hanasaki.azusa.common.EventPublisher

/**
 * 基于 Spring ApplicationEventPublisher 的事件发布器
 * 支持 @TransactionalEventListener 实现事务感知的事件处理
 */
@Component
class SpringEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher,
) : EventPublisher {

    override fun publish(event: DomainEvent) {
        applicationEventPublisher.publishEvent(event)
    }

    override fun publishAll(events: Collection<DomainEvent>) {
        events.forEach { applicationEventPublisher.publishEvent(it) }
    }
}