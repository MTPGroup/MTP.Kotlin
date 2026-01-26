package tech.hanasaki.azusa.common.kernel.model

@JvmInline
value class AvatarUrl(
    val value: String,
) {
    init {
        require(value.startsWith("http://") || value.startsWith("https://")) { "非法URL" }
    }
}
