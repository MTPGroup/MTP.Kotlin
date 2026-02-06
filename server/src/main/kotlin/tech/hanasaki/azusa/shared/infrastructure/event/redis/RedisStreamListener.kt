package tech.hanasaki.azusa.shared.infrastructure.event.redis

import io.lettuce.core.*
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import tech.hanasaki.azusa.shared.domain.event.DomainEvent
import tech.hanasaki.azusa.shared.port.`in`.EventSubscriber
import tech.hanasaki.azusa.shared.port.out.EventSerializerPort

@OptIn(ExperimentalLettuceCoroutinesApi::class)
class RedisStreamListener(
    private val redisCommands: RedisCoroutinesCommands<String, String>,
    private val config: StreamConfig,
    private val eventSerializer: EventSerializerPort,
) : EventSubscriber {
    private val logger = LoggerFactory.getLogger(RedisStreamListener::class.java)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val handlers = mutableMapOf<String, MutableList<suspend (DomainEvent) -> Unit>>()

    @Volatile
    private var running = false

    private var listeningJob: Job? = null

    override fun registerHandler(eventType: String, handler: suspend (DomainEvent) -> Unit) {
        logger.info("注册事件处理器: $eventType")
        handlers.computeIfAbsent(eventType) {
            mutableListOf()
        }.add(handler)
    }

    suspend fun start() {
        if (running) {
            logger.warn("Redis Stream监听器正在运行!")
            return
        }

        ensureConsumerGroupExists()

        running = true
        logger.info("启动Redis Stream监听器: ${config.streamKey}")

        listeningJob = scope.launch {
            while (isActive && running) {
                try {
                    val processed = onMessage()
                    if (!processed) {
                        delay(100)
                    }
                } catch (e: Exception) {
                    logger.error("在消息循环中发生错误: ${e.message}", e)
                    delay(config.pollInterval)
                }
            }
        }
    }

    fun stop() {
        if (!running) return
        running = false
        logger.info("正在停止Redis Stream监听器")
        listeningJob?.cancel()
        scope.cancel()
    }

    private suspend fun ensureConsumerGroupExists() {
        try {
            redisCommands.xgroupCreate(
                XReadArgs.StreamOffset.latest(config.streamKey),
                config.consumerGroup,
                XGroupCreateArgs.Builder.mkstream()
            )
            logger.info("创建消费者组: ${config.consumerGroup}")
        } catch (e: Exception) {
            if (e.message?.contains("BUSYGROUP") == true) {
                logger.info("消费者组 ${config.consumerGroup} 已存在")
            } else {
                throw e
            }
        }
    }

    private suspend fun onMessage(): Boolean {
        try {
            val consumer = Consumer.from(config.consumerGroup, config.consumerName)
            // 使用 Flow 接收消息
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

                    if (eventType != null && payload != null) {
                        val eventHandlers = handlers[eventType]
                        if (!eventHandlers.isNullOrEmpty()) {
                            val event = eventSerializer.deserialize(payload)

                            supervisorScope {
                                eventHandlers.forEach { handler ->
                                    try {
                                        handler(event)
                                    } catch (e: Exception) {
                                        logger.error("处理器执行失败 [$eventType]: ${e.message}", e)
                                    }
                                }
                                // 确认消息
                                redisCommands.xack(config.streamKey, config.consumerGroup, streamMessage.id)
                                logger.debug("成功分发事件: $eventType 到 ${eventHandlers.size} 个处理器, ID: ${streamMessage.id}")
                            }
                        } else {
                            logger.warn("未找到事件处理器: $eventType, ID: ${streamMessage.id}")
                            // 即使没有处理器，也应该 ACK，否则消息会一直堆积？
                            // 或者应该放入死信队列？这里暂时 ACK 以避免阻塞
                            redisCommands.xack(config.streamKey, config.consumerGroup, streamMessage.id)
                        }
                    } else {
                        logger.warn("收到格式错误的消息: ${streamMessage.id}")
                        redisCommands.xack(config.streamKey, config.consumerGroup, streamMessage.id)
                    }
                } catch (e: Exception) {
                    logger.error("处理消息失败 ${streamMessage.id}: ${e.message}", e)
                    // 这里的异常处理策略：
                    // 1. 重试？
                    // 2. ACK 并记录日志（丢弃）？
                    // 3. 不 ACK（Redis 会重新投递，可能导致死循环）？
                    // 目前选择不 ACK，依赖 Redis 的 Pending 机制后续处理（需要另外的死信处理逻辑）
                }
            }

            return messageCount > 0
        } catch (e: Exception) {
            logger.error("读取流失败: ${e.message}", e)
            return false
        }
    }
}