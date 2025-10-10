package tech.hanasaki.momotalk_plus.core.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val image: String?,
    val createdAt: String?,
    val updatedAt: String?,
)