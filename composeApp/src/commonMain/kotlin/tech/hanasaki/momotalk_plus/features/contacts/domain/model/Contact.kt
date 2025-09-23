package tech.hanasaki.momotalk_plus.features.contacts.domain.model

import kotlinx.serialization.Serializable
import tech.hanasaki.momotalk_plus.core.domain.model.Visibility

@Serializable
data class Contact(
    val id: String,
    val creatorId: String,
    val name: String,
    val signature: String,
    val persona: String,
    val avatarUrl: String,
    val visibility: Visibility,
    val createdAt: String,
    val updatedAt: String,
)