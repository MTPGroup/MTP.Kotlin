package tech.hanasaki.azusa.shared.port.out

import tech.hanasaki.azusa.shared.infrastructure.event.outbox.OutboxEvent
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * Outbox 事件仓储接口
 */

interface OutboxEventRepositoryPort {
    /**
     * 保存事件到 Outbox
     */
    suspend fun save(event: OutboxEvent)

    /**
     * 批量保存事件
     */
    suspend fun saveAll(events: Collection<OutboxEvent>)

    /**
     * 查找未发布的事件
     * @param limit 最大返回数量
     * @param maxRetries 最大重试次数（仅对 FAILED 事件生效）
     */
    suspend fun findPending(limit: Int = 100, maxRetries: Int = 3): List<OutboxEvent>

    /**
     * 标记事件为已发布
     */
    suspend fun markAsSent(id: Uuid)

    /**
     * 标记事件发布失败
     */
    suspend fun markAsFailed(id: Uuid, error: String)

    /**
     * 批量标记为已发布
     */
    suspend fun markAllAsPublished(ids: Collection<Uuid>)

    /**
     * 批量标记为发布失败
     */
    suspend fun markAllAsFailed(ids: Collection<Uuid>)

    /**
     * 删除已发布且早于指定时间的事件（用于清理）
     */
    suspend fun deletePublishedBefore(before: Instant): Int
}