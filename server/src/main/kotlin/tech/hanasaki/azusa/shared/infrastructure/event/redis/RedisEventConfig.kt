package tech.hanasaki.azusa.shared.infrastructure.event.redis

import io.ktor.server.config.*
import tech.hanasaki.azusa.shared.infrastructure.config.optionalInt
import tech.hanasaki.azusa.shared.infrastructure.config.optionalLong
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
    )
}
