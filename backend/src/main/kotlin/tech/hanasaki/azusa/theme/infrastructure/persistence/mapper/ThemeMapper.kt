package tech.hanasaki.azusa.theme.infrastructure.persistence.mapper

import org.springframework.stereotype.Component
import tech.hanasaki.azusa.shared.ThemeId
import tech.hanasaki.azusa.shared.UserId
import tech.hanasaki.azusa.theme.domain.model.Theme
import tech.hanasaki.azusa.theme.infrastructure.persistence.entity.ThemeEntity
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Component
class ThemeMapper {
    fun toEntity(theme: Theme, isNewRecord: Boolean = false): ThemeEntity = ThemeEntity(
        id = theme.id.value,
        authorId = theme.authorId.value,
        name = theme.name,
        description = theme.description,
        previewUrl = theme.previewUrl,
        data = theme.data,
        downloadCount = theme.downloadCount,
        version = theme.version,
        createdAt = theme.createdAt.toJavaInstant(),
        updatedAt = theme.updatedAt.toJavaInstant(),
    ).apply {
        this.isNewRecord = isNewRecord
    }

    fun toDomain(entity: ThemeEntity): Theme = Theme(
        id = ThemeId(entity.id),
        authorId = UserId(entity.authorId),
        name = entity.name,
        description = entity.description,
        previewUrl = entity.previewUrl,
        data = entity.data,
        downloadCount = entity.downloadCount,
        version = entity.version,
        createdAt = entity.createdAt.toKotlinInstant(),
        updatedAt = entity.updatedAt.toKotlinInstant(),
    )
}
