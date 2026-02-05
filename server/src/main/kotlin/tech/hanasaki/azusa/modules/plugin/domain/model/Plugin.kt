package tech.hanasaki.azusa.modules.plugin.domain.model

import tech.hanasaki.azusa.common.domain.model.AggregateRoot
import tech.hanasaki.azusa.common.domain.model.PluginId
import tech.hanasaki.azusa.common.domain.model.UserId
import tech.hanasaki.azusa.modules.plugin.domain.events.PluginApproved
import tech.hanasaki.azusa.modules.plugin.domain.events.PluginCreated
import tech.hanasaki.azusa.modules.plugin.domain.events.PluginRejected
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * 插件聚合根
 */
class Plugin private constructor(
    val id: PluginId,
    var name: String,
    var description: String,
    var version: String,
    var schema: PluginSchema,
    var code: String,
    val authorId: UserId,
    var status: PluginStatus,
    var likeCount: Int,
    val createdAt: Instant,
    var updatedAt: Instant,
) : AggregateRoot() {

    companion object {
        /**
         * 创建新插件（状态为 PENDING）
         */
        fun create(
            name: String,
            description: String,
            version: String,
            schema: PluginSchema,
            code: String,
            authorId: UserId,
        ): Plugin {
            val now = Clock.System.now()
            val plugin = Plugin(
                id = PluginId(Uuid.random()),
                name = name,
                description = description,
                version = version,
                schema = schema,
                code = code,
                authorId = authorId,
                status = PluginStatus.PENDING,
                likeCount = 0,
                createdAt = now,
                updatedAt = now,
            )
            plugin.addDomainEvent(
                PluginCreated(
                    pluginId = plugin.id,
                    authorId = authorId,
                    name = name,
                )
            )
            return plugin
        }

        /**
         * 从持久化层重建插件（不触发事件）
         */
        fun reconstitute(
            id: PluginId,
            name: String,
            description: String,
            version: String,
            schema: PluginSchema,
            code: String,
            authorId: UserId,
            status: PluginStatus,
            likeCount: Int,
            createdAt: Instant,
            updatedAt: Instant,
        ): Plugin = Plugin(
            id = id,
            name = name,
            description = description,
            version = version,
            schema = schema,
            code = code,
            authorId = authorId,
            status = status,
            likeCount = likeCount,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    /**
     * 更新插件信息
     */
    fun update(
        name: String,
        description: String,
        version: String,
        schema: PluginSchema,
        code: String,
    ) {
        this.name = name
        this.description = description
        this.version = version
        this.schema = schema
        this.code = code
        this.updatedAt = Clock.System.now()
        // 修改后需要重新审核
        if (this.status == PluginStatus.APPROVED) {
            this.status = PluginStatus.PENDING
        }
    }

    /**
     * 审批通过
     */
    fun approve() {
        if (status != PluginStatus.PENDING) {
            throw IllegalStateException("Only pending plugins can be approved")
        }
        status = PluginStatus.APPROVED
        updatedAt = Clock.System.now()
        addDomainEvent(
            PluginApproved(
                pluginId = id,
                authorId = authorId,
            )
        )
    }

    /**
     * 审批拒绝
     */
    fun reject() {
        if (status != PluginStatus.PENDING) {
            throw IllegalStateException("Only pending plugins can be rejected")
        }
        status = PluginStatus.REJECTED
        updatedAt = Clock.System.now()
        addDomainEvent(
            PluginRejected(
                pluginId = id,
                authorId = authorId,
            )
        )
    }

    /**
     * 归档
     */
    fun archive() {
        status = PluginStatus.ARCHIVED
        updatedAt = Clock.System.now()
    }

    /**
     * 增加点赞数
     */
    fun incrementLikeCount() {
        likeCount++
    }

    /**
     * 减少点赞数
     */
    fun decrementLikeCount() {
        if (likeCount > 0) {
            likeCount--
        }
    }
}
