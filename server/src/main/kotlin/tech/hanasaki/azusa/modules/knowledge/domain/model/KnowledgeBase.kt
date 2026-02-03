package tech.hanasaki.azusa.modules.knowledge.domain.model

import tech.hanasaki.azusa.common.kernel.base.AggregateRoot
import tech.hanasaki.azusa.common.kernel.model.KnowledgeBaseId
import tech.hanasaki.azusa.common.kernel.model.UserId
import tech.hanasaki.azusa.modules.knowledge.domain.events.KnowledgeBaseCreatedEvent
import tech.hanasaki.azusa.modules.knowledge.domain.events.KnowledgeBaseDeletedEvent
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * 知识库聚合根
 */
data class KnowledgeBase(
    val id: KnowledgeBaseId,
    var name: String,
    var description: String?,
    val authorId: UserId,
    var isPublic: Boolean,
    val createdAt: Instant,
    var updatedAt: Instant,
) : AggregateRoot() {

    companion object {
        /**
         * 创建新知识库
         */
        fun create(
            name: String,
            description: String?,
            authorId: UserId,
            isPublic: Boolean = false,
        ): KnowledgeBase {
            val now = Clock.System.now()
            val knowledgeBase = KnowledgeBase(
                id = KnowledgeBaseId(Uuid.random()),
                name = name,
                description = description,
                authorId = authorId,
                isPublic = isPublic,
                createdAt = now,
                updatedAt = now,
            )
            knowledgeBase.addDomainEvent(
                KnowledgeBaseCreatedEvent(
                    knowledgeBaseId = knowledgeBase.id,
                    authorId = authorId,
                    name = name,
                )
            )
            return knowledgeBase
        }

        /**
         * 从持久化层重建（不触发事件）
         */
        fun reconstitute(
            id: KnowledgeBaseId,
            name: String,
            description: String?,
            authorId: UserId,
            isPublic: Boolean,
            createdAt: Instant,
            updatedAt: Instant,
        ): KnowledgeBase = KnowledgeBase(
            id = id,
            name = name,
            description = description,
            authorId = authorId,
            isPublic = isPublic,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    /**
     * 更新知识库信息
     */
    fun update(
        name: String,
        description: String?,
        isPublic: Boolean,
    ) {
        this.name = name
        this.description = description
        this.isPublic = isPublic
        this.updatedAt = Clock.System.now()
    }

    /**
     * 标记删除
     */
    fun markDeleted() {
        addDomainEvent(
            KnowledgeBaseDeletedEvent(
                knowledgeBaseId = id,
                authorId = authorId,
            )
        )
    }
}
