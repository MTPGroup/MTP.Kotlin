package tech.hanasaki.momotalk_plus.core.data.datasource.local.entity

data class CharacterEntity(
    val id: String,
    val creatorId: String,
    val name: String,
    val signature: String,
    val persona: String,
    val avatarUrl: String,
    val visibility: String,
    val createdAt: String,
    val updatedAt: String,
    val creatorName: String,
    val creatorImage: String?,
)
