package tech.hanasaki.azusa.modules.plugin.domain.model

import tech.hanasaki.azusa.shared.domain.model.vo.PluginId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import kotlin.time.Instant

/**
 * 用户对插件的点赞
 */
data class PluginLike(
    val userId: UserId,
    val pluginId: PluginId,
    val createdAt: Instant,
)
