package tech.hanasaki.azusa.common.domain.model

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class Email(val value: String) {
    init {
        require(value.contains('@')) { "Invalid email format" }
    }
}
