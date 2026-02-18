package tech.hanasaki.azusa.shared.infrastructure.event.redis

import io.github.oshai.kotlinlogging.KotlinLogging
import io.lettuce.core.*
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import kotlinx.coroutines.*
import tech.hanasaki.azusa.shared.domain.event.IntegrationEvent
import tech.hanasaki.azusa.shared.port.`in`.EventSubscriberPort
import tech.hanasaki.azusa.shared.port.out.EventSerializerPort

@OptIn(ExperimentalLettuceCoroutinesApi::class)
class RedisStreamListener(
    private val redisCommands: RedisCoroutinesCommands<String, String>,
    private val config: StreamConfig,
    private val eventSerializer: EventSerializerPort,
) : EventSubscriberPort {
    private val logger = KotlinLogging.logger { }
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val handlers = mutableMapOf<String, MutableList<suspend (IntegrationEvent) -> Unit>>()

    @Volatile
    private var running = false

    private var listeningJob: Job? = null

    override fun registerHandler(eventType: String, handler: suspend (IntegrationEvent) -> Unit) {
        handlers.computeIfAbsent(eventType) {
            mutableListOf()
        }.add(handler)
    }

    suspend fun start() {
        if (running) {
            logger.warn { "Redis Stream监听器正在运行!" }
            return
        }

        ensureConsumerGroupExists()
        claimPendingMessages()

        running = true
        logger.info { "启动Redis Stream监听器: ${config.streamKey}" }

        listeningJob = scope.launch {
            while (isActive && running) {
                try {
                    val processed = onMessage()
                    if (!processed) {
                        delay(100)
                    }
                } catch (_: CancellationException) {
                    break
                } catch (e: Exception) {
                    if (!running) break
                    logger.error(e) { "在消息循环中发生错误: ${e.message}" }
                    delay(config.pollInterval)
                }
            }
        }
    }

    suspend fun stop() {
        if (!running) return
        running = false
        logger.info { "正在停止Redis Stream监听器" }
        listeningJob?.cancelAndJoin()
        scope.cancel()
    }

    private suspend fun ensureConsumerGroupExists() {
        try {
            redisCommands.xgroupCreate(
                XReadArgs.StreamOffset.latest(config.streamKey),
                config.consumerGroup,
                XGroupCreateArgs.Builder.mkstream()
            )
            logger.info { "创建消费者组: ${config.consumerGroup}" }
        } catch (e: Exception) {
            if (e.message?.contains("BUSYGROUP") == true) {
                logger.info { "消费者组 ${config.consumerGroup} 已存在" }
            } else {
                throw e
            }
        }
    }

    private suspend fun onMessage(): Boolean {
        val consumer = Consumer.from(config.consumerGroup, config.consumerName)
        val messagesFlow = redisCommands.xreadgroup(
            consumer,
            XReadArgs.Builder.count(config.batchSize.toLong())
                .block(config.pollInterval.inWholeMilliseconds),
            XReadArgs.StreamOffset.lastConsumed(config.streamKey)
        )

        var messageCount = 0
        messagesFlow.collect { streamMessage: StreamMessage<String, String> ->
            messageCount++
            val bodyMap = streamMessage.body

            try {
                val eventType = bodyMap["eventType"]
                val payload = bodyMap["payload"]

                logger.debug { "收到消息: eventType: $eventType, payload: $payload" }

                if (eventType != null && payload != null) {
                    val eventHandlers = handlers[eventType]
                    logger.info { "查找 handler: eventType=$eventType, 找到 ${eventHandlers?.size ?: 0} 个" }
                    if (!eventHandlers.isNullOrEmpty()) {
                        val event = eventSerializer.deserialize(payload)
                        logger.info { "反序列化成功: event=${event::class.simpleName}" }

                        supervisorScope {
                            val handlerResults = eventHandlers.map { handler ->
                                logger.info { "准备调用 handler: ${handler::class.simpleName}" }
                                try {
                                    handler(event)
                                    true
                                } catch (e: Exception) {
                                    logger.error(e) { "处理器执行失败 [$eventType]: ${e.message}" }
                                    false
                                }
                            }

                            val allSucceeded = handlerResults.all { it }
                            if (allSucceeded) {
                                redisCommands.xack(config.streamKey, config.consumerGroup, streamMessage.id)
                                logger.debug { "成功分发事件: $eventType 到 ${eventHandlers.size} 个处理器, ID: ${streamMessage.id}" }
                            } else {
                                logger.error { "部分处理器失败，消息未确认 [$eventType]: ${streamMessage.id}" }
                            }
                        }
                    } else {
                        logger.warn { "未找到事件处理器: $eventType, ID: ${streamMessage.id}" }
                        redisCommands.xack(config.streamKey, config.consumerGroup, streamMessage.id)
                    }
                } else {
                    logger.warn { "收到格式错误的消息: ${streamMessage.id}" }
                    redisCommands.xack(config.streamKey, config.consumerGroup, streamMessage.id)
                }
            } catch (e: Exception) {
                logger.error(e) { "处理消息失败 ${streamMessage.id}: ${e.message}" }
            }
        }

        return messageCount > 0
    }

    private suspend fun claimPendingMessages() {
        try {
            logger.info { "检查待认领消息..." }

            val consumer = Consumer.from(config.consumerGroup, config.consumerName)
            val xAutoClaimArgs = XAutoClaimArgs.Builder
                .xautoclaim(consumer, config.claimIdleTime.inWholeMilliseconds, "0-0")
                .count(config.claimBatchSize.toLong())
            val claimedMessages = redisCommands.xautoclaim(config.streamKey, xAutoClaimArgs)

            var claimCount = 0
            claimedMessages?.messages?.forEach { message ->
                claimCount++
                logger.info { "认领消息: ${message.id} 从死消费者" }
                processClaimedMessage(message)
            }

            if (claimCount > 0) {
                logger.info { "成功认领并处理 $claimCount 条消息" }
            }
        } catch (e: Exception) {
            logger.error(e) { "认领待处理消息失败: ${e.message}" }
            // Don't crash - we'll try again on next startup
        }
    }

    /**
     * Process a claimed message with retry counting
     */
    private suspend fun processClaimedMessage(message: StreamMessage<String, String>) {
        val bodyMap = message.body
        val eventType = bodyMap["eventType"]
        val payload = bodyMap["payload"]

        if (eventType == null || payload == null) {
            logger.warn { "认领的消息格式错误: ${message.id}" }
            redisCommands.xack(config.streamKey, config.consumerGroup, message.id)
            return
        }

        // Check retry count
        val retryKey = "azusa:retry:${message.id}"
        val retryCount = (redisCommands.hget(retryKey, "count")?.toIntOrNull() ?: 0)

        logger.info { "处理认领消息: ${message.id}, 类型: $eventType, 重试次数: $retryCount" }

        if (retryCount >= config.maxRetries) {
            logger.error { "消息 ${message.id} 超过最大重试次数 ($retryCount/${config.maxRetries})，移至死信队列" }
            moveToDLQ(message, eventType, payload, retryCount, "超过最大重试次数")
            return
        }

        // Increment retry counter
        redisCommands.hincrby(retryKey, "count", 1)
        redisCommands.hset(retryKey, "lastAttempt", System.currentTimeMillis().toString())
        redisCommands.hset(retryKey, "eventType", eventType)

        try {
            val eventHandlers = handlers[eventType]
            if (!eventHandlers.isNullOrEmpty()) {
                val event = eventSerializer.deserialize(payload)

                supervisorScope {
                    val handlerResults = eventHandlers.map { handler ->
                        try {
                            handler(event)
                            true
                        } catch (e: Exception) {
                            logger.error(e) { "处理器执行失败 [$eventType]: ${e.message}" }
                            false
                        }
                    }

                    val allSucceeded = handlerResults.all { it }
                    if (allSucceeded) {
                        redisCommands.xack(config.streamKey, config.consumerGroup, message.id)
                        redisCommands.del(retryKey) // Clean up retry counter
                        logger.info { "认领消息处理成功: ${message.id}" }
                    } else {
                        logger.error { "认领消息处理失败，保留在待处理队列: ${message.id}" }
                        // Don't ACK - will be reclaimed on next startup
                    }
                }
            } else {
                logger.warn { "未找到事件处理器: $eventType, 消息: ${message.id}" }
                redisCommands.xack(config.streamKey, config.consumerGroup, message.id)
                redisCommands.del(retryKey)
            }
        } catch (e: Exception) {
            logger.error(e) { "处理认领消息 ${message.id} 失败: ${e.message}" }
            // Don't ACK - will be reclaimed
        }
    }

    /**
     * Move message to Dead Letter Queue
     */
    private suspend fun moveToDLQ(
        message: StreamMessage<String, String>,
        eventType: String,
        payload: String,
        retryCount: Int,
        reason: String,
    ) {
        if (!config.dlqEnabled || config.dlqStreamKey == null) {
            logger.warn { "死信队列未启用，消息 ${message.id} 将被丢弃" }
            redisCommands.xack(config.streamKey, config.consumerGroup, message.id)
            return
        }

        try {
            // Add to DLQ with metadata
            redisCommands.xadd(
                config.dlqStreamKey,
                mapOf(
                    "originalEventType" to eventType,
                    "originalPayload" to payload,
                    "originalId" to message.id,
                    "retryCount" to retryCount.toString(),
                    "failureReason" to reason,
                    "timestamp" to System.currentTimeMillis().toString(),
                    "consumerGroup" to config.consumerGroup
                )
            )

            // ACK original message to remove from pending
            redisCommands.xack(config.streamKey, config.consumerGroup, message.id)

            // Clean up retry counter
            redisCommands.del("azusa:retry:${message.id}")

            logger.error { "消息已移至死信队列: ${message.id} -> ${config.dlqStreamKey}" }
        } catch (e: Exception) {
            logger.error(e) { "移动消息到死信队列失败: ${message.id}" }
            // Don't ACK original - will be retried
        }
    }
}
