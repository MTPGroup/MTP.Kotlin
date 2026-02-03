package tech.hanasaki.azusa.common.platform.event.outbox.repository

import tech.hanasaki.azusa.common.platform.event.outbox.model.OutboxEvent
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Outbox 事件仓储接口
 */

interface OutboxEventRepository {
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
     */
    suspend fun findUnpublished(limit: Int = 100): List<OutboxEvent>

    /**
     * 标记事件为已发布
     */
    suspend fun markAsPublished(eventId: Uuid, publishedAt: Instant)

    /**
     * 标记事件发布失败
     */
    suspend fun markAsFailed(eventId: Uuid)

    /**
     * 批量标记为已发布
     */
    suspend fun markAllAsPublished(eventIds: Collection<Uuid>, publishedAt: Instant)

    /**
     * 批量标记为发布失败
     */
    suspend fun markAllAsFailed(eventIds: Collection<Uuid>)

    /**
     * 删除已发布且早于指定时间的事件（用于清理）
     */
    suspend fun deletePublishedBefore(before: Instant): Int
}
