package tech.hanasaki.azusa.shared.infrastructure.event.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.update
import tech.hanasaki.azusa.shared.domain.event.DomainEvent
import tech.hanasaki.azusa.shared.domain.event.EventListener
import tech.hanasaki.azusa.shared.domain.event.EventPublisher
import tech.hanasaki.azusa.shared.domain.event.EventSubscriber
import tech.hanasaki.azusa.shared.infrastructure.database.OutboxEventTable
import tech.hanasaki.azusa.shared.infrastructure.database.dbQuery
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Clock

/**
 * 基于 Kotlin SharedFlow 的内存事件总线
 *
 * 支持：
 * - 事件发布与订阅
 * - Outbox 模式持久化
 * - 类型安全的事件监听
 */
class InMemoryEventBus : EventPublisher, EventSubscriber {

    private val _events = MutableSharedFlow<DomainEvent>(extraBufferCapacity = 100)
    private val listeners = ConcurrentHashMap<Class<*>, MutableList<EventListener<*>>>()

    val events = _events.asSharedFlow()

    // ==================== EventPublisher ====================

    override suspend fun publish(event: DomainEvent) {
        val outboxId = persistOutboxEvent(event)
        _events.emit(event)
        notifyListeners(event)
        // 发布成功后标记为已完成
        markAsCompleted(outboxId)
    }

    override suspend fun publishAll(events: Collection<DomainEvent>) {
        val outboxIds = persistOutboxEvents(events)
        events.forEach { event ->
            _events.emit(event)
            notifyListeners(event)
        }
        // 发布成功后标记为已完成
        markAllAsCompleted(outboxIds)
    }

    // ==================== EventSubscriber ====================

    override fun <T : DomainEvent> subscribe(eventType: Class<T>, listener: EventListener<T>) {
        listeners.computeIfAbsent(eventType) { mutableListOf() }.add(listener)
    }

    /**
     * 订阅事件（内联版本，支持类型推断）
     */
    inline fun <reified T : DomainEvent> subscribe(listener: EventListener<T>) {
        subscribe(T::class.java, listener)
    }

    /**
     * 订阅事件并在指定协程作用域中处理
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

    /**
     * 取消订阅
     */
    fun <T : DomainEvent> unsubscribe(eventType: Class<T>, listener: EventListener<T>) {
        listeners[eventType]?.remove(listener)
    }

    /**
     * 取消订阅（内联版本）
     */
    inline fun <reified T : DomainEvent> unsubscribe(listener: EventListener<T>) {
        unsubscribe(T::class.java, listener)
    }

    // ==================== Internal ====================

    /**
     * 仅发送事件到内存总线，不持久化
     * 用于 OutboxPoller 重发已持久化的事件
     */
    internal suspend fun emitWithoutPersist(event: DomainEvent) {
        _events.emit(event)
        notifyListeners(event)
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun notifyListeners(event: DomainEvent) {
        val eventListeners = listeners[event::class.java] as? List<EventListener<DomainEvent>> ?: return
        eventListeners.forEach { listener ->
            try {
                listener.handle(event)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun persistOutboxEvent(event: DomainEvent): UUID = dbQuery {
        val eventId = UUID.randomUUID()
        val now = Clock.System.now()
        OutboxEventTable.insert {
            it[id] = eventId
            it[eventType] = EventRegistry.getTypeName(event)
            it[listenerId] = DEFAULT_LISTENER_ID
            it[publicationDate] = now
            it[serializedEvent] = EventJson.serialize(event)
            it[completionDate] = null
        }
        eventId
    }

    private suspend fun persistOutboxEvents(events: Collection<DomainEvent>): List<UUID> = dbQuery {
        val now = Clock.System.now()
        events.map { event ->
            val eventId = UUID.randomUUID()
            OutboxEventTable.insert {
                it[id] = eventId
                it[eventType] = EventRegistry.getTypeName(event)
                it[listenerId] = DEFAULT_LISTENER_ID
                it[publicationDate] = now
                it[serializedEvent] = EventJson.serialize(event)
                it[completionDate] = null
            }
            eventId
        }
    }

    private suspend fun markAsCompleted(eventId: UUID): Unit = dbQuery {
        val now = Clock.System.now()
        OutboxEventTable.update({ OutboxEventTable.id eq eventId }) {
            it[completionDate] = now
        }
    }

    private suspend fun markAllAsCompleted(eventIds: List<UUID>): Unit = dbQuery {
        val now = Clock.System.now()
        eventIds.forEach { eventId ->
            OutboxEventTable.update({ OutboxEventTable.id eq eventId }) {
                it[completionDate] = now
            }
        }
    }

    private companion object {
        private const val DEFAULT_LISTENER_ID = "outbox"
    }
}
