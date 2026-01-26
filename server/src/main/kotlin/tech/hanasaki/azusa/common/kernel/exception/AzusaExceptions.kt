package tech.hanasaki.azusa.common.kernel.exception

abstract class AzusaException(
    override val message: String,
    override val cause: Throwable? = null,
) : RuntimeException(message, cause)

class NotFoundException(
    message: String = "Resource not found",
) : AzusaException(message)

class ConflictException(
    message: String = "Resource already exists",
) : AzusaException(message)

class AuthenticationException(
    message: String = "Authentication failed",
) : AzusaException(message)

class AuthorizationException(
    message: String = "Access denied",
) : AzusaException(message)

class DomainException(
    message: String,
) : AzusaException(message)