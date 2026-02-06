package tech.hanasaki.azusa.shared.domain.model.vo

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class Email(val value: String) {
    init {
        require(value.isNotBlank()) { "邮件不能为空 " }
        require(value.contains('@')) { "非法的邮件格式" }
    }
}