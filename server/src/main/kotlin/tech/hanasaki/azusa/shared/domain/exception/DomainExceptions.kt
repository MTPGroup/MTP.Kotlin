package tech.hanasaki.azusa.shared.domain.exception

sealed class DomainException(
    open val code: String,
    override val message: String,
) : RuntimeException(message)

class ValidationException(
    code: String = "VALIDATION_ERROR",
    message: String = "验证失败",
    val details: Map<String, String> = emptyMap(),
) : DomainException(code, message)

class AuthenticationException(
    code: String = "AUTHENTICATION_FAILED",
    message: String = "认证失败",
) : DomainException(code, message)

class AuthorizationException(
    code: String = "AUTHORIZATION_FAILED",
    message: String = "无权限",
) : DomainException(code, message)

class ConflictException(
    code: String = "CONFLICT",
    message: String = "资源冲突",
) : DomainException(code, message)

class NotFoundException(
    code: String = "NOT_FOUND",
    message: String = "资源不存在",
) : DomainException(code, message)

class HitLimitException(
    code: String = "HIT_LIMIT",
    message: String = "请求过于频繁",
    val retryAfter: Long,
) : DomainException(code, message)

class InternalServerException(
    code: String = "INTERNAL_SERVER_ERROR",
    message: String = "服务器内部错误",
) : DomainException(code, message)