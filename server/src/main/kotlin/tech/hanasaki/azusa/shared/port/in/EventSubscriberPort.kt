package tech.hanasaki.azusa.shared.port.`in`

import tech.hanasaki.azusa.shared.domain.event.DomainEvent

interface EventSubscriber {
    /**
     * 订阅特定类型的事件
     */
    fun registerHandler(
        eventType: String,
        handler: suspend (DomainEvent) -> Unit,
    )
}

inline fun <reified T : DomainEvent> EventSubscriber.subscribe(
    eventType: String,
    handler: EventHandlerPort<T>,
) {
    registerHandler(eventType) {
        if (it is T) {
            handler(it)
        }
    }
}
