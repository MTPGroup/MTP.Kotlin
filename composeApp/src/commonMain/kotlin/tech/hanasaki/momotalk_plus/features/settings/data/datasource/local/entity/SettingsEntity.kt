package tech.hanasaki.momotalk_plus.features.settings.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val theme: String,
    val language: String,
    val notificationsEnabled: Boolean,
    val soundEnabled: Boolean,
    val vibrationEnabled: Boolean,
    val chatBackgroundUrl: String? = null,
    val contactBackgroundUrl: String? = null,
    val createdAt: String,
    val updatedAt: String,
)
