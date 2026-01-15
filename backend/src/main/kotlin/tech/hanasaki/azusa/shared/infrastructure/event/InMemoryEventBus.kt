package tech.hanasaki.azusa.shared.infrastructure.event


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import tech.hanasaki.azusa.shared.domain.event.DomainEvent
import tech.hanasaki.azusa.shared.domain.event.EventPublisher

/**
 * 基于 Kotlin SharedFlow 的内存事件总线
 */
@Component
class InMemoryEventBus : EventPublisher {

    private val _events = MutableSharedFlow<DomainEvent>(extraBufferCapacity = 100)

    val events = _events.asSharedFlow()

    override suspend fun publish(event: DomainEvent) {
        _events.emit(event)
    }

    override suspend fun publishAll(events: Collection<DomainEvent>) {
        events.forEach { _events.emit(it) }
    }

    private inline fun <reified T : DomainEvent> subscribe(
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
