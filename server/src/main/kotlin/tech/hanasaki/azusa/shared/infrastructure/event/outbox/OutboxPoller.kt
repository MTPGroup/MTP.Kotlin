package tech.hanasaki.azusa.shared.infrastructure.event.outbox

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import tech.hanasaki.azusa.shared.domain.event.DomainEvent
import tech.hanasaki.azusa.shared.domain.event.OutboxEventRepository
import tech.hanasaki.azusa.shared.infrastructure.event.bus.EventJson
import tech.hanasaki.azusa.shared.infrastructure.event.bus.InMemoryEventBus
import kotlin.time.Clock

/**
 * Outbox 事件轮询器
 */
class OutboxPoller(
    private val outboxRepository: OutboxEventRepository,
    private val eventBus: InMemoryEventBus,
    private val config: OutboxPollerConfig,
) {
    private val logger = LoggerFactory.getLogger(OutboxPoller::class.java)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pollingJob: Job? = null
    private var cleanupJob: Job? = null

    @Volatile
    private var running = false

    /**
     * 启动轮询器
     */
    fun start() {
        if (running) {
            logger.warn("OutboxPoller is already running")
            return
        }
        running = true
        logger.info("Starting OutboxPoller with polling interval: ${config.pollingInterval}")

        pollingJob = scope.launch {
            while (isActive && running) {
                try {
                    pollAndPublish()
                } catch (e: Exception) {
                    logger.error("Error during outbox polling", e)
                }
                delay(config.pollingInterval)
            }
        }

        if (config.cleanupEnabled) {
            cleanupJob = scope.launch {
                while (isActive && running) {
                    delay(config.cleanupInterval)
                    try {
                        cleanup()
                    } catch (e: Exception) {
                        logger.error("Error during outbox cleanup", e)
                    }
                }
            }
        }
    }

    /**
     * 停止轮询器
     */
    fun stop() {
        if (!running) return
        running = false
        logger.info("Stopping OutboxPoller")
        pollingJob?.cancel()
        cleanupJob?.cancel()
        scope.cancel()
    }

    /**
     * 手动触发一次轮询
     */
    suspend fun pollOnce() {
        pollAndPublish()
    }

    private suspend fun pollAndPublish() {
        val unpublishedEvents = outboxRepository.findUnpublished(config.batchSize)
        if (unpublishedEvents.isEmpty()) return

        logger.debug("Found ${unpublishedEvents.size} unpublished events")
        val now = Clock.System.now()
        val publishedIds = mutableListOf<java.util.UUID>()

        for (outboxEvent in unpublishedEvents) {
            try {
                val domainEvent = EventJson.deserialize(outboxEvent.eventType, outboxEvent.payload)
                if (domainEvent != null) {
                    emitWithoutPersist(domainEvent)
                    publishedIds.add(outboxEvent.id)
                    logger.debug("Re-published event: ${outboxEvent.eventType}")
                } else {
                    logger.warn("Unknown event type: ${outboxEvent.eventType}, marking as published to skip")
                    publishedIds.add(outboxEvent.id)
                }
            } catch (e: Exception) {
                logger.error("Failed to re-publish event ${outboxEvent.id}: ${e.message}")
            }
        }

        if (publishedIds.isNotEmpty()) {
            outboxRepository.markAllAsPublished(publishedIds, now)
            logger.info("Marked ${publishedIds.size} events as published")
        }
    }

    private suspend fun emitWithoutPersist(event: DomainEvent) {
        eventBus.emitWithoutPersist(event)
    }

    private suspend fun cleanup() {
        val cutoffTime = Clock.System.now() - config.retentionPeriod
        val deletedCount = outboxRepository.deletePublishedBefore(cutoffTime)
        if (deletedCount > 0) {
            logger.info("Cleaned up $deletedCount old published events")
        }
    }
}
