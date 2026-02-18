package tech.hanasaki.azusa.modules.plugin.domain.model

/**
 * 插件状态
 */
enum class PluginStatus {
    PENDING,    // 待审核
    APPROVED,   // 已通过
    REJECTED,   // 已拒绝
    ARCHIVED;   // 已归档

    companion object {
        fun fromString(value: String): PluginStatus = when (value.trim().lowercase()) {
            "pending" -> PENDING
            "approved" -> APPROVED
            "rejected" -> REJECTED
            "archived" -> ARCHIVED
            else -> throw IllegalArgumentException("Unknown plugin status: $value")
        }
    }
}
