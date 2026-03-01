package tech.hanasaki.azusa.shared.domain.exception

sealed class DomainException(
    open val code: String,
    override val message: String,
) : RuntimeException(message)

class ValidationException(
    message: String = "验证失败",
    code: String = ErrorCodes.VALIDATION_ERROR,
    val details: Map<String, String> = emptyMap(),
) : DomainException(code, message)

class AuthenticationException(
    message: String = "认证失败",
    code: String = ErrorCodes.AUTHENTICATION_FAILED,
) : DomainException(code, message)

class AuthorizationException(
    message: String = "无权限",
    code: String = ErrorCodes.AUTHORIZATION_FAILED,
) : DomainException(code, message)

class ConflictException(
    message: String = "资源冲突",
    code: String = ErrorCodes.CONFLICT,
) : DomainException(code, message)

class NotFoundException(
    message: String = "资源不存在",
    code: String = ErrorCodes.NOT_FOUND,
) : DomainException(code, message)

class HitLimitException(
    message: String = "请求过于频繁",
    code: String = ErrorCodes.HIT_LIMIT,
    val retryAfter: Long,
) : DomainException(code, message)

class InternalServerException(
    message: String = "服务器内部错误",
    code: String = ErrorCodes.INTERNAL_SERVER_ERROR,
) : DomainException(code, message)