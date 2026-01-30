package tech.hanasaki.azusa.common.platform.event.listener

import io.lettuce.core.Consumer
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.XReadArgs
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import tech.hanasaki.azusa.common.kernel.event.StreamListener

@OptIn(ExperimentalLettuceCoroutinesApi::class)
class RedisStreamListener(
    private val redis: RedisCoroutinesCommands<String, String>,
    private val config: StreamConfig,
) : StreamListener {
    private val logger = LoggerFactory.getLogger(RedisStreamListener::class.java)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val handlers = mutableMapOf<String, suspend (Map<String, String>) -> Unit>()

    @Volatile
    private var running = false

    private var listeningJob: Job? = null

    /**
     * 注册处理器
     */
    fun registerHandler(eventType: String, handler: suspend (Map<String, String>) -> Unit) {
        handlers[eventType] = handler
        logger.info("Registered handler for event type: $eventType")
    }

    override suspend fun onMessage(): Boolean {
        try {
            val consumer = Consumer.from(config.consumerGroup, config.consumerName)
            val messagesFlow = redis.xreadgroup(
                consumer,
                XReadArgs.Builder.count(config.batchSize.toLong())
                    .block(config.pollInterval.inWholeMilliseconds),
                XReadArgs.StreamOffset.lastConsumed(config.streamKey)
            )

            var messageCount = 0
            messagesFlow.collect { streamMessage: io.lettuce.core.StreamMessage<String, String> ->
                messageCount++
                val stream = streamMessage.stream
                val bodyMap = streamMessage.body.toMap()
                try {
                    val eventType = bodyMap["type"]
                    if (eventType != null) {
                        handlers[eventType]?.invoke(bodyMap)
                        redis.xack(stream, config.consumerGroup, streamMessage.id)
                        logger.debug("Processed and acknowledged event: $eventType, messageId: ${streamMessage.id}")
                    } else {
                        logger.warn("Received message without type: ${streamMessage.id}")
                    }
                } catch (e: Exception) {
                    logger.error("Error processing message ${streamMessage.id}: ${e.message}", e)
                }
            }

            return messageCount > 0
        } catch (e: Exception) {
            logger.error("Error reading from stream: ${e.message}", e)
            return true
        }
    }

    override suspend fun start() {
        if (running) {
            logger.warn("RedisStreamListener is already running")
            return
        }

        try {
            ensureConsumerGroupExists()
        } catch (e: Exception) {
            logger.error("Failed to create consumer group: ${e.message}", e)
            throw e
        }

        running = true
        logger.info("Starting RedisStreamListener for stream: ${config.streamKey}")

        listeningJob = scope.launch {
            while (isActive && running) {
                try {
                    onMessage()
                } catch (e: Exception) {
                    logger.error("Error in message loop: ${e.message}", e)
                    delay(config.pollInterval)
                }
            }
        }

        logger.info("RedisStreamListener started successfully")
    }

    fun stop() {
        if (!running) return
        running = false
        logger.info("Stopping RedisStreamListener")
        listeningJob?.cancel()
        scope.cancel()
    }

    private suspend fun ensureConsumerGroupExists() {
        try {
            redis.xgroupCreate(
                XReadArgs.StreamOffset.latest(config.streamKey),
                config.consumerGroup
            )
            logger.info("Created consumer group: ${config.consumerGroup}")
        } catch (e: Exception) {
            if (e.message?.contains("BUSYGROUP") == true) {
                logger.info("Consumer group ${config.consumerGroup} already exists")
            } else if (e.message?.contains("no such key") == true) {
                try {
                    redis.xadd(config.streamKey, mapOf("init" to "1"))
                    redis.xgroupCreate(
                        XReadArgs.StreamOffset.latest(config.streamKey),
                        config.consumerGroup
                    )
                    logger.info("Created stream and consumer group: ${config.consumerGroup}")
                } catch (ex: Exception) {
                    logger.error("Failed to create stream and consumer group", ex)
                    throw ex
                }
            } else {
                throw e
            }
        }
    }
}
