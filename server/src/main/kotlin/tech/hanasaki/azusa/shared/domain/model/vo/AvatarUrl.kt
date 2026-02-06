package tech.hanasaki.azusa.shared.domain.model.vo

@JvmInline
value class AvatarUrl(
    val value: String,
) {
    init {
        require(value.startsWith("http://") || value.startsWith("https://")) { "非法URL" }
    }
}