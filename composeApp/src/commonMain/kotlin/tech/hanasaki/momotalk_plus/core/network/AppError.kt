package tech.hanasaki.momotalk_plus.core.network

sealed interface AppError {
    val message: String

    data class Validation(
        override val message: String,
        val details: Map<String, String>? = null,
        val code: String? = null,
    ) : AppError

    data class Unauthorized(
        override val message: String = "未登录或令牌无效",
        val code: String? = null,
    ) : AppError

    data class Forbidden(
        override val message: String = "权限不足",
        val code: String? = null,
    ) : AppError

    data class NotFound(
        override val message: String = "资源不存在",
        val code: String? = null,
    ) : AppError

    data class Conflict(
        override val message: String = "资源冲突",
        val code: String? = null,
    ) : AppError

    data class RateLimited(
        override val message: String = "请求过于频繁",
        val retryAfter: Long? = null,
        val code: String? = null,
    ) : AppError

    data class Http(
        val status: Int,
        override val message: String,
        val code: String? = null,
        val details: Map<String, String>? = null,
    ) : AppError

    data class Network(
        override val message: String = "网络连接失败，请检查网络",
        val causeMessage: String? = null,
    ) : AppError

    data class Serialization(
        override val message: String = "数据解析失败",
        val causeMessage: String? = null,
    ) : AppError

    data class Unknown(
        override val message: String = "未知错误",
        val causeMessage: String? = null,
    ) : AppError
}
