package tech.hanasaki.azusa.shared.infrastructure.event.outbox

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import tech.hanasaki.azusa.shared.port.out.EventPublisherPort
import tech.hanasaki.azusa.shared.port.out.EventSerializerPort
import tech.hanasaki.azusa.shared.port.out.OutboxEventRepositoryPort
import tech.hanasaki.azusa.shared.port.out.TransactionalPort
import kotlin.time.Clock

/**
 * Outbox 事件轮询器
 */
class OutboxPoller(
    private val repository: OutboxEventRepositoryPort,
    private val eventSerializer: EventSerializerPort,
    private val eventPublisher: EventPublisherPort,
    private val tx: TransactionalPort,
    private val outboxConfig: OutboxPollerConfig,
) {
    private val logger = LoggerFactory.getLogger(OutboxPoller::class.java)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pollingJob: Job? = null
    private var cleanupJob: Job? = null

    @Volatile
    private var running = false

    fun start() {
        if (running) {
            logger.warn("Outbox轮询器正在运行!")
            return
        }
        running = true
        logger.info("启动轮询器，轮询间隔: ${outboxConfig.pollingInterval}")

        pollingJob = scope.launch {
            while (isActive && running) {
                try {
                    processBatch()
                    delay(outboxConfig.pollingInterval)
                } catch (e: Exception) {
                    logger.error("在轮询过程中发生错误: ", e)
                }
            }
        }

        if (outboxConfig.cleanupEnabled) {
            cleanupJob = scope.launch {
                while (isActive && running) {
                    delay(outboxConfig.cleanupInterval)
                    try {
                        cleanup()
                    } catch (e: Exception) {
                        logger.error("在轮询器清理过程中发生错误: ", e)
                    }
                }
            }
        }
    }

    fun stop() {
        if (!running) return
        running = false
        logger.info("正在停止轮询器...")
        pollingJob?.cancel()
        cleanupJob?.cancel()
        scope.cancel()
    }

    private suspend fun processBatch() {
        tx.execute {
            val events = repository.findPending(outboxConfig.batchSize)

            events.forEach { event ->
                try {
                    val domainEvent = eventSerializer.deserialize(event.payload)
                    eventPublisher.publish(domainEvent)
                    repository.markAsSent(event.id)
                    logger.debug("处理消息成功: ${event.eventType}")
                } catch (e: Exception) {
                    logger.error("处理消息失败: ${event.eventType}", e)
                    repository.markAsFailed(event.id, e.message ?: "未知错误")
                }
            }
        }
    }

    private suspend fun cleanup() {
        tx.execute {
            val cutoffTime = Clock.System.now() - outboxConfig.retentionPeriod
            val deletedCount =
                repository.deletePublishedBefore(cutoffTime)
            if (deletedCount > 0) {
                logger.info("清理 $deletedCount 个旧的已发布事件")
            }
        }
    }
}
