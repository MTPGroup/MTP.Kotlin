package tech.hanasaki.azusa.common.platform.event.outbox

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import tech.hanasaki.azusa.common.kernel.event.IntegrationEvent
import tech.hanasaki.azusa.common.platform.event.outbox.repository.OutboxEventRepository
import java.util.*
import kotlin.time.Clock

/**
 * Outbox 事件轮询器
 */
class OutboxPoller(
    private val outboxRepository: OutboxEventRepository,
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
        val publishedIds = mutableListOf<UUID>()

        for (outboxEvent in unpublishedEvents) {
            try {
//                publishToBus(integrationEvent)
                publishedIds.add(outboxEvent.id)
                logger.debug("Re-published event: ${outboxEvent.eventType}")
            } catch (e: Exception) {
                logger.error("Failed to re-publish event ${outboxEvent.id}: ${e.message}")
            }
        }

        if (publishedIds.isNotEmpty()) {
            outboxRepository.markAllAsPublished(publishedIds, now)
            logger.info("Marked ${publishedIds.size} events as published")
        }
    }

    private suspend fun publishToBus(event: IntegrationEvent) {
        // TODO: 使用Redis Stream发布集成事件
    }

    private suspend fun cleanup() {
        val cutoffTime = Clock.System.now() - config.retentionPeriod
        val deletedCount = outboxRepository.deletePublishedBefore(cutoffTime)
        if (deletedCount > 0) {
            logger.info("Cleaned up $deletedCount old published events")
        }
    }
}
