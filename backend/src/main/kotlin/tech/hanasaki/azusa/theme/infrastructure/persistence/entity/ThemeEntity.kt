package tech.hanasaki.azusa.theme.infrastructure.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import tech.hanasaki.azusa.theme.domain.model.ThemeDefinition
import java.time.Instant
import java.util.*

@Table("themes")
data class ThemeEntity(
    @Id
    @get:JvmName("getThemeId")
    val id: UUID,
    val authorId: UUID,
    val name: String,
    val description: String?,
    val previewUrl: String?,
    val data: ThemeDefinition,
    val downloadCount: Int,
    val version: String,
    val createdAt: Instant,
    val updatedAt: Instant,
) : Persistable<UUID> {
    @Transient
    var isNewRecord: Boolean = false

    override fun getId(): UUID = id
    override fun isNew(): Boolean = isNewRecord
}