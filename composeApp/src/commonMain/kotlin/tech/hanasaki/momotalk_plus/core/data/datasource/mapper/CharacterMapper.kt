package tech.hanasaki.momotalk_plus.core.data.datasource.mapper

import tech.hanasaki.momotalk_plus.core.data.datasource.local.entity.CharacterEntity
import tech.hanasaki.momotalk_plus.core.domain.model.Character
import tech.hanasaki.momotalk_plus.core.domain.model.Creator
import tech.hanasaki.momotalk_plus.core.domain.model.Visibility

/**
 * Character Mapper - 数据库实体与领域模型之间的转换
 */
object CharacterMapper {

    /**
     * 将数据库实体转换为领域模型
     */
    fun CharacterEntity.toCharacter(): Character {
        return Character(
            id = id,
            creatorId = creatorId,
            name = name,
            signature = signature,
            persona = persona,
            avatarUrl = avatarUrl,
            visibility = when (visibility) {
                "public" -> Visibility.PUBLIC
                "private" -> Visibility.PRIVATE
                else -> Visibility.PUBLIC
            },
            createdAt = createdAt,
            updatedAt = updatedAt,
            creator = Creator(
                id = creatorId,
                name = creatorName,
                image = creatorImage,
                username = null
            )
        )
    }

    /**
     * 将领域模型转换为数据库实体参数
     */
    fun Character.toCharacterEntity(): CharacterEntity {
        return CharacterEntity(
            id = id,
            creatorId = creatorId,
            name = name,
            signature = signature,
            persona = persona,
            avatarUrl = avatarUrl,
            visibility = visibility.toString(),
            createdAt = createdAt,
            updatedAt = updatedAt,
            creatorName = creator.name,
            creatorImage = creator.image,
        )
    }
}

