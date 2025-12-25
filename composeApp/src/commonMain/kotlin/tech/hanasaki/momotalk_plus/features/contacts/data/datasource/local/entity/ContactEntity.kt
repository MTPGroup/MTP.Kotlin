package tech.hanasaki.momotalk_plus.features.contacts.data.datasource.local.entity

data class ContactEntity(
    val id: String,
    val creatorId: String,
    val name: String,
    val signature: String,
    val persona: String,
    val avatarUrl: String,
    val visibility: String,
    val createdAt: String,
    val updatedAt: String,
)
