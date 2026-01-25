package tech.hanasaki.azusa.shared.domain.event

import java.util.*
import kotlin.time.Instant

/**
 * Outbox 事件实体 - 用于持久化领域事件
 */
data class OutboxEvent(
    val id: UUID,
    val eventType: String,
    val payload: String,
    val occurredAt: Instant,
    val publishedAt: Instant? = null,
) {
    val isPublished: Boolean
        get() = publishedAt != null

    fun markAsPublished(at: Instant): OutboxEvent = copy(publishedAt = at)
}

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
    suspend fun markAsPublished(eventId: UUID, publishedAt: Instant)

    /**
     * 批量标记为已发布
     */
    suspend fun markAllAsPublished(eventIds: Collection<UUID>, publishedAt: Instant)

    /**
     * 删除已发布且早于指定时间的事件（用于清理）
     */
    suspend fun deletePublishedBefore(before: Instant): Int
}
