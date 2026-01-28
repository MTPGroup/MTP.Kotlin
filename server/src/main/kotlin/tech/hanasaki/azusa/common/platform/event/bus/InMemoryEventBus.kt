package tech.hanasaki.azusa.common.platform.event.bus

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import tech.hanasaki.azusa.common.kernel.event.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 基于 Kotlin SharedFlow 和 内存监听列表 的事件总线
 * 支持同步（事务内）和异步（最终一致性）两种订阅模式
 */
class InMemoryEventBus : EventPublisher, EventSubscriber {
    private val logger = LoggerFactory.getLogger(InMemoryEventBus::class.java)

    private val _events = MutableSharedFlow<DomainEvent>(extraBufferCapacity = 100)
    val events = _events.asSharedFlow()

    private val listeners = ConcurrentHashMap<Class<*>, CopyOnWriteArrayList<WrappedListener<*>>>()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private data class WrappedListener<T : DomainEvent>(
        val listener: EventListener<T>,
        val mode: SubscriptionMode,
    )

    override suspend fun publish(event: DomainEvent) {
        val eventType = event::class.java

        listeners.forEach { (type, list) ->
            if (type.isAssignableFrom(eventType)) {
                list.forEach { wrapped ->
                    dispatch(wrapped, event)
                }
            }
        }

        _events.emit(event)
    }

    private suspend fun <T : DomainEvent> dispatch(wrapped: WrappedListener<*>, event: T) {
        @Suppress("UNCHECKED_CAST")
        val listener = wrapped.listener as EventListener<T>

        if (wrapped.mode == SubscriptionMode.SYNCHRONOUS) {
            listener.handle(event)
        } else {
            scope.launch {
                try {
                    listener.handle(event)
                } catch (e: Exception) {
                    logger.error(
                        "Error handling async event ${event::class.simpleName} by listener ${listener::class.simpleName}",
                        e
                    )
                }
            }
        }
    }

    override suspend fun publishAll(events: Collection<DomainEvent>) {
        events.forEach { publish(it) }
    }

    override suspend fun <T : DomainEvent> subscribe(
        eventType: Class<T>,
        listener: EventListener<T>,
        mode: SubscriptionMode,
    ) {
        listeners.computeIfAbsent(eventType) { CopyOnWriteArrayList() }
            .add(WrappedListener(listener, mode))
    }

    /**
     * 基于 Flow 的异步订阅
     */
    inline fun <reified T : DomainEvent> subscribe(
        scope: CoroutineScope,
        crossinline handler: suspend (T) -> Unit,
    ) {
        scope.launch(Dispatchers.IO) {
            events
                .filterIsInstance<T>()
                .collect { event ->
                    try {
                        handler(event)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        }
    }
}
