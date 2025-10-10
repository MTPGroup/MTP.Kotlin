package tech.hanasaki.momotalk_plus.features.contacts.data.datasource.mapper

import tech.hanasaki.momotalk_plus.core.domain.model.Visibility
import tech.hanasaki.momotalk_plus.features.contacts.data.datasource.local.entity.ContactEntity
import tech.hanasaki.momotalk_plus.features.contacts.domain.model.Contact

object ContactMapper {
    fun ContactEntity.toContact(): Contact =
        Contact(
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
        )

    fun Contact.toContactEntity(): ContactEntity =
        ContactEntity(
            id = id,
            creatorId = creatorId,
            name = name,
            signature = signature,
            persona = persona,
            avatarUrl = avatarUrl,
            visibility = when (visibility) {
                Visibility.PUBLIC -> "public"
                Visibility.PRIVATE -> "private"
            },
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}