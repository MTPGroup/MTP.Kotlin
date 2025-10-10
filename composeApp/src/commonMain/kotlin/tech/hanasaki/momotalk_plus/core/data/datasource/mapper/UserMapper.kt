package tech.hanasaki.momotalk_plus.core.data.datasource.mapper

import tech.hanasaki.momotalk_plus.core.data.datasource.local.entity.UserEntity
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.dto.UserProfile
import tech.hanasaki.momotalk_plus.core.domain.model.User


object UserMapper {
    fun UserProfile.toUser(): User =
        User(
            uid = id,
            name = name,
            email = email,
            avatar = image,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    fun UserEntity.toUser(): User =
        User(
            uid = id,
            name = name,
            email = email,
            avatar = image,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    fun User.toUserEntity(): UserEntity =
        UserEntity(
            id = uid,
            name = name,
            email = email,
            image = avatar,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

}