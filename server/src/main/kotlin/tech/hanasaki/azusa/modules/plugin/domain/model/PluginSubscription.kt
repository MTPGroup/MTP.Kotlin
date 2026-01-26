package tech.hanasaki.azusa.modules.plugin.domain.model

import tech.hanasaki.azusa.shared.domain.model.PluginId
import tech.hanasaki.azusa.shared.domain.model.UserId
import kotlin.time.Instant

/**
 * 用户订阅的插件
 */
data class PluginSubscription(
    val userId: UserId,
    val pluginId: PluginId,
    /** 是否启用 */
    val isActive: Boolean,
    val subscribedAt: Instant,
)
