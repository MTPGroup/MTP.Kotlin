package tech.hanasaki.azusa.common.platform.event.outbox

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import tech.hanasaki.azusa.common.platform.event.LettuceDistLock
import tech.hanasaki.azusa.common.platform.event.listener.StreamConfig
import tech.hanasaki.azusa.common.platform.event.outbox.model.OutboxEvent
import tech.hanasaki.azusa.common.platform.event.outbox.repository.OutboxEventRepository
import tech.hanasaki.azusa.common.platform.util.AppJson
import java.util.*
import kotlin.time.Clock

/**
 * Outbox 事件轮询器
 */
@OptIn(ExperimentalLettuceCoroutinesApi::class)
class OutboxPoller(
    private val outboxRepository: OutboxEventRepository,
    private val outboxConfig: OutboxPollerConfig,
    private val streamConfig: StreamConfig,
    private val redis: RedisCoroutinesCommands<String, String>,
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
        logger.info("Starting OutboxPoller with polling interval: ${outboxConfig.pollingInterval}")

        pollingJob = scope.launch {
            while (isActive && running) {
                try {
                    pollAndPublish()
                } catch (e: Exception) {
                    logger.error("Error during outbox polling", e)
                }
                delay(outboxConfig.pollingInterval)
            }
        }

        if (outboxConfig.cleanupEnabled) {
            cleanupJob = scope.launch {
                while (isActive && running) {
                    delay(outboxConfig.cleanupInterval)
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
        val lock = LettuceDistLock(
            redis,
            "lock:outbox_poller"
        )

        if (lock.tryLock()) {
            try {
                val unpublishedEvents = outboxRepository.findUnpublished(outboxConfig.batchSize)
                if (unpublishedEvents.isEmpty()) {
                    lock.unlock()
                    return
                }

                logger.debug("Found ${unpublishedEvents.size} unpublished events")
                val now = Clock.System.now()
                val publishedIds = mutableListOf<UUID>()
                val failedIds = mutableListOf<UUID>()

                unpublishedEvents.forEach { event ->
                    try {
                        publishToBus(event)
                        publishedIds.add(event.id)
                        logger.debug("Re-published event: ${event.eventType}")
                    } catch (_: Exception) {
                        failedIds.add(event.id)
                        logger.error("Failed to re-publish event ${event.id}: ${event.eventType}")
                    }
                }

                if (publishedIds.isNotEmpty()) {
                    try {
                        outboxRepository.markAllAsPublished(publishedIds, now)
                        logger.info("Marked ${publishedIds.size} events as published")
                    } catch (_: Exception) {
                        logger.error("Failed to mark events as published")
                    }
                }

                if (failedIds.isNotEmpty()) {
                    try {
                        outboxRepository.markAllAsFailed(failedIds)
                        logger.warn("Marked ${failedIds.size} events as failed")
                    } catch (_: Exception) {
                        logger.error("Failed to mark events as failed")
                    }
                }

            } catch (e: Exception) {
                logger.error("Critical error in pollAndPublish loop", e)
            } finally {
                lock.unlock()
            }
        }


    }

    private suspend fun publishToBus(event: OutboxEvent) {
        val body = mapOf(
            "type" to event.eventType,
            "payload" to AppJson.json.encodeToString(event.payload),
            "eventId" to event.id.toString()
        )

        val streamId = redis.xadd(streamConfig.streamKey, body)
        logger.info("Publishing event: $streamId")
    }

    private suspend fun cleanup() {
        val cutoffTime = Clock.System.now() - outboxConfig.retentionPeriod
        val deletedCount = outboxRepository.deletePublishedBefore(cutoffTime)
        if (deletedCount > 0) {
            logger.info("Cleaned up $deletedCount old published events")
        }
    }
}
