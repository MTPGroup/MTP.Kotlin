package tech.hanasaki.momotalk_plus.features.contacts.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ContactEntity(
    @PrimaryKey val id: String,
    val creatorId: String,
    val name: String,
    val signature: String,
    val persona: String,
    val avatarUrl: String,
    val visibility: String,
    val createdAt: String,
    val updatedAt: String,
)
