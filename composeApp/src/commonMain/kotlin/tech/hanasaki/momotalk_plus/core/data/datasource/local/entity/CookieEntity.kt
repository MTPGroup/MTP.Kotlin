package tech.hanasaki.momotalk_plus.core.data.datasource.local.entity

import androidx.room.Entity

@Entity(primaryKeys = ["name", "domain", "path"])
data class CookieEntity(
    val name: String,
    val value: String,
    val maxAge: Int,
    val expires: String,
    val domain: String,
    val path: String,
    val secure: Boolean,
    val httpOnly: Boolean,
    val extensions: String,
)
