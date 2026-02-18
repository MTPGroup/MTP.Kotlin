package tech.hanasaki.azusa.shared.domain.exception

sealed class DomainException(
    open val code: String,
    override val message: String,
) : RuntimeException(message)

class ValidationException(
    message: String = "验证失败",
    code: String = "VALIDATION_ERROR",
    val details: Map<String, String> = emptyMap(),
) : DomainException(code, message)

class AuthenticationException(
    message: String = "认证失败",
    code: String = "AUTHENTICATION_FAILED",
) : DomainException(code, message)

class AuthorizationException(
    message: String = "无权限",
    code: String = "AUTHORIZATION_FAILED",
) : DomainException(code, message)

class ConflictException(
    message: String = "资源冲突",
    code: String = "CONFLICT",
) : DomainException(code, message)

class NotFoundException(
    message: String = "资源不存在",
    code: String = "NOT_FOUND",
) : DomainException(code, message)

class HitLimitException(
    message: String = "请求过于频繁",
    code: String = "HIT_LIMIT",
    val retryAfter: Long,
) : DomainException(code, message)

class InternalServerException(
    message: String = "服务器内部错误",
    code: String = "INTERNAL_SERVER_ERROR",
) : DomainException(code, message)