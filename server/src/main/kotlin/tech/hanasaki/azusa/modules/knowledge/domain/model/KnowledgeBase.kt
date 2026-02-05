package tech.hanasaki.azusa.modules.knowledge.domain.model

import tech.hanasaki.azusa.common.domain.model.AggregateRoot
import tech.hanasaki.azusa.common.domain.model.KnowledgeBaseId
import tech.hanasaki.azusa.common.domain.model.UserId
import tech.hanasaki.azusa.modules.knowledge.domain.events.KnowledgeBaseCreated
import tech.hanasaki.azusa.modules.knowledge.domain.events.KnowledgeBaseDeleted
import kotlin.time.Clock
import kotlin.time.Instant
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
                KnowledgeBaseCreated(
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
            KnowledgeBaseDeleted(
                knowledgeBaseId = id,
                authorId = authorId,
            )
        )
    }
}
