package tech.hanasaki.azusa.modules.theme.domain.model

import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.common.kernel.model.ThemeId
import tech.hanasaki.azusa.common.kernel.model.UserId
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

@Serializable
data class ThemeDefinition(
    val lightColors: Map<String, String>,
    val darkColors: Map<String, String>,
    val roundness: Int = 4,
) {
    companion object {
        val DEFAULT = ThemeDefinition(emptyMap(), emptyMap())
    }
}
