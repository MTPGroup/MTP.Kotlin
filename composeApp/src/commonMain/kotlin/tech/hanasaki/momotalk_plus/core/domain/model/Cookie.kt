package tech.hanasaki.momotalk_plus.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SerializableCookie(
    val name: String,
    val value: String,
    val maxAge: Int? = null,
    val expires: String? = null,
    val domain: String? = null,
    val path: String? = null,
    val secure: Boolean = false,
    val httpOnly: Boolean = false,
    val extensions: Map<String, String?> = emptyMap(),
)
