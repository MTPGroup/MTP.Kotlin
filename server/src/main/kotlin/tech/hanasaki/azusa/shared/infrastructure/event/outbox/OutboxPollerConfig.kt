package tech.hanasaki.azusa.shared.infrastructure.event.outbox

import io.ktor.server.config.*
import tech.hanasaki.azusa.shared.infrastructure.config.optionalBoolean
import tech.hanasaki.azusa.shared.infrastructure.config.optionalInt
import tech.hanasaki.azusa.shared.infrastructure.config.optionalLong
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Outbox 轮询器配置
 */
data class OutboxPollerConfig(
    /** 轮询间隔 */
    val pollingInterval: Duration = 30.seconds,
    /** 每次轮询的批量大小 */
    val batchSize: Int = 100,
    /** 是否启用清理任务 */
    val cleanupEnabled: Boolean = true,
    /** 清理任务执行间隔 */
    val cleanupInterval: Duration = 1.minutes,
    /** 已发布事件保留时间 */
    val retentionPeriod: Duration = 7.days,
)

/**
 * 从配置文件读取 OutboxPoller 配置
 *
 * application.yaml 示例:
 * ```yaml
 * event:
 *   outbox:
 *     pollingIntervalSeconds: 30
 *     batchSize: 100
 *     cleanupEnabled: true
 *     cleanupIntervalMinutes: 1
 *     retentionDays: 7
 * ```
 */
fun ApplicationConfig.readOutboxPollerConfig(): OutboxPollerConfig {
    val prefix = "event.outbox"
    return OutboxPollerConfig(
        pollingInterval = optionalLong("$prefix.pollingIntervalSeconds")?.seconds ?: 30.seconds,
        batchSize = optionalInt("$prefix.batchSize") ?: 100,
        cleanupEnabled = optionalBoolean("$prefix.cleanupEnabled") ?: true,
        cleanupInterval = optionalLong("$prefix.cleanupIntervalMinutes")?.minutes ?: 1.minutes,
        retentionPeriod = optionalLong("$prefix.retentionDays")?.days ?: 7.days,
    )
}
