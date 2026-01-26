package tech.hanasaki.azusa.theme.domain.model

import tech.hanasaki.azusa.common.ThemeId
import tech.hanasaki.azusa.common.UserId
import kotlin.time.Instant


data class Theme(
    val id: ThemeId,
    val authorId: UserId,
    val name: String,
    val description: String?,
    val previewUrl: String?,
    val data: ThemeDefinition,
    val downloadCount: Int,
    val version: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)

/**
 * 主题的具体定义（颜色、形状等）
 * 这是一个 Value Object，对应数据库中的 JSONB 字段
 */
data class ThemeDefinition(
    val lightColors: Map<String, String>,
    val darkColors: Map<String, String>,
    val roundness: Int = 4,
) {
    companion object {
        val DEFAULT = ThemeDefinition(emptyMap(), emptyMap())
    }
}