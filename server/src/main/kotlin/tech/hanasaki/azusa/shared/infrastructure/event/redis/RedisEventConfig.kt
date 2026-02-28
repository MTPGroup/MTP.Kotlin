package tech.hanasaki.azusa.shared.infrastructure.event.redis

import io.ktor.server.config.*
import tech.hanasaki.azusa.shared.infrastructure.config.optionalBoolean
import tech.hanasaki.azusa.shared.infrastructure.config.optionalInt
import tech.hanasaki.azusa.shared.infrastructure.config.optionalLong
import tech.hanasaki.azusa.shared.infrastructure.config.optionalString
import tech.hanasaki.azusa.shared.infrastructure.config.requireString
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Redis Stream 配置
 */
data class StreamConfig(
    /** Stream key */
    val streamKey: String,
    /** 消费者组名称 */
    val consumerGroup: String,
    /** 消费者名称（通常为服务实例 ID） */
    val consumerName: String,
    /** 每次读取的批量大小 */
    val batchSize: Int = 10,
    /** 轮询间隔 */
    val pollInterval: Duration = 1.seconds,
    /** 消息认领空闲时间（超过此时间未确认的消息可被其他消费者认领） */
    val claimIdleTime: Duration = 30.seconds,
    /** 每次认领消息的批量大小 */
    val claimBatchSize: Int = 100,
    /** 周期性认领检查间隔 */
    val claimInterval: Duration = 10.seconds,
    /** 消息处理失败最大重试次数 */
    val maxRetries: Int = 3,
    /** 是否启用死信队列 */
    val dlqEnabled: Boolean = true,
    /** 死信队列 Stream key */
    val dlqStreamKey: String? = "azusa:dlq:outbox-events",
)

/**
 * 从配置文件读取 Stream 配置
 *
 * application.yaml 示例:
 * ```yaml
 * event:
 *   stream:
 *     streamKey: "azusa:stream:outbox-events"
 *     consumerGroup: "azusa-consumers"
 *     consumerName: "azusa-instance-1"
 *     batchSize: 10
 *     pollIntervalSeconds: 1
 *     claimIdleTimeSeconds: 30
 *     claimBatchSize: 100
 *     claimIntervalSeconds: 10
 *     maxRetries: 3
 *     dlqEnabled: true
 *     dlqStreamKey: "azusa:dlq:outbox-events"
 * ```
 */
fun ApplicationConfig.readStreamConfig(): StreamConfig {
    val prefix = "event.stream"
    return StreamConfig(
        streamKey = requireString("$prefix.streamKey"),
        consumerGroup = requireString("$prefix.consumerGroup"),
        consumerName = requireString("$prefix.consumerName"),
        batchSize = optionalInt("$prefix.batchSize") ?: 10,
        pollInterval = optionalLong("$prefix.pollIntervalSeconds")?.seconds ?: 1.seconds,
        claimIdleTime = optionalLong("$prefix.claimIdleTimeSeconds")?.seconds ?: 30.seconds,
        claimBatchSize = optionalInt("$prefix.claimBatchSize") ?: 100,
        claimInterval = optionalLong("$prefix.claimIntervalSeconds")?.seconds ?: 10.seconds,
        maxRetries = optionalInt("$prefix.maxRetries") ?: 3,
        dlqEnabled = optionalBoolean("$prefix.dlqEnabled") ?: true,
        dlqStreamKey = optionalString("$prefix.dlqStreamKey") ?: "azusa:dlq:outbox-events",
    )
}
