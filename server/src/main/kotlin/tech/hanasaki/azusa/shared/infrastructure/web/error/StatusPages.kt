package tech.hanasaki.azusa.shared.infrastructure.web.error

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import tech.hanasaki.azusa.shared.domain.exception.*
import tech.hanasaki.azusa.shared.domain.exception.NotFoundException
import tech.hanasaki.azusa.shared.infrastructure.web.response.ApiResponse

private val logger = KotlinLogging.logger { }

fun Application.configureErrorHandling() {
    install(StatusPages) {
        exception<ValidationException> { call, exception ->
            logger.warn { "验证失败: ${exception.message}" }
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse.error<Unit>(
                    message = exception.message,
                    code = ErrorCodes.VALIDATION_ERROR,
                    errors = exception.details
                )
            )
        }

        exception<IllegalArgumentException> { call, exception ->
            logger.warn { "参数错误: ${exception.message}" }
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse.error<Unit>(
                    message = exception.message ?: "请求参数错误",
                    code = ErrorCodes.BAD_REQUEST
                )
            )
        }

        exception<IllegalStateException> { call, exception ->
            logger.warn { "状态冲突: ${exception.message}" }
            call.respond(
                HttpStatusCode.Conflict,
                ApiResponse.error<Unit>(
                    message = exception.message ?: "状态冲突",
                    code = ErrorCodes.CONFLICT
                )
            )
        }

        exception<ContentTransformationException> { call, exception ->
            logger.warn { "请求体解析失败: ${exception.message}" }
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse.error<Unit>(
                    message = "请求体格式错误",
                    code = ErrorCodes.BAD_REQUEST_BODY
                )
            )
        }

        exception<BadRequestException> { call, exception ->
            logger.warn { "Bad request: ${exception.message}" }
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse.error<Unit>(
                    message = exception.message ?: "请求参数错误",
                    code = ErrorCodes.BAD_REQUEST
                )
            )
        }

        exception<AuthenticationException> { call, exception ->
            logger.warn { "认证失败: ${exception.message}" }
            call.respond(
                HttpStatusCode.Unauthorized,
                ApiResponse.error<Unit>(
                    message = exception.message,
                    code = ErrorCodes.AUTHENTICATION_FAILED
                )
            )
        }

        exception<AuthorizationException> { call, exception ->
            logger.warn { "权限不足: ${exception.message}" }
            call.respond(
                HttpStatusCode.Forbidden,
                ApiResponse.error<Unit>(
                    message = exception.message,
                    code = ErrorCodes.AUTHORIZATION_FAILED
                )
            )
        }

        exception<ConflictException> { call, exception ->
            logger.warn { "资源冲突: ${exception.message}" }
            call.respond(
                HttpStatusCode.Conflict,
                ApiResponse.error<Unit>(
                    message = exception.message,
                    code = ErrorCodes.CONFLICT
                )
            )
        }

        exception<NotFoundException> { call, exception ->
            logger.warn { "资源未找到: ${exception.message}" }
            call.respond(
                HttpStatusCode.NotFound,
                ApiResponse.error<Unit>(
                    message = exception.message,
                    code = ErrorCodes.NOT_FOUND
                )
            )
        }

        exception<HitLimitException> { call, exception ->
            logger.warn { "请求过于频繁: ${exception.message}" }
            call.response.headers.append(HttpHeaders.RetryAfter, exception.retryAfter.toString())
            call.respond(
                HttpStatusCode.TooManyRequests,
                ApiResponse.error<Unit>(
                    message = exception.message,
                    code = ErrorCodes.HIT_LIMIT
                )
            )
        }

        exception<DomainException> { call, exception ->
            logger.error { "领域错误: ${exception.code} - ${exception.message}" }
            val statusCode = exception.toHttpStatusCode()
            call.respond(
                statusCode,
                ApiResponse.error<Unit>(
                    message = exception.message,
                    code = exception.code
                )
            )
        }

        exception<org.jetbrains.exposed.v1.exceptions.ExposedSQLException> { call, exception ->
            if (exception.message?.contains("could not serialize access") == true) {
                logger.warn { "数据库并发冲突: ${exception.message}" }
                call.respond(
                    HttpStatusCode.Conflict,
                    ApiResponse.error<Unit>(
                        message = "操作冲突，请重试",
                        code = ErrorCodes.CONCURRENT_CONFLICT
                    )
                )
            } else {
                logger.error(exception) { "数据库错误" }
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse.error<Unit>(
                        message = "服务器内部错误",
                        code = ErrorCodes.INTERNAL_SERVER_ERROR
                    )
                )
            }
        }

        exception<kotlinx.coroutines.CancellationException> { _, exception ->
            logger.debug { "请求已取消: ${exception.message}" }
            throw exception
        }

        exception<Throwable> { call, exception ->
            logger.error(exception) { "未知错误" }
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse.error<Unit>(
                    message = "服务器内部错误",
                    code = ErrorCodes.INTERNAL_SERVER_ERROR
                )
            )
        }
    }
}

fun DomainException.toHttpStatusCode(): HttpStatusCode = when (this) {
    is ValidationException -> HttpStatusCode.BadRequest
    is AuthenticationException -> HttpStatusCode.Unauthorized
    is AuthorizationException -> HttpStatusCode.Forbidden
    is NotFoundException -> HttpStatusCode.NotFound
    is ConflictException -> HttpStatusCode.Conflict
    is HitLimitException -> HttpStatusCode.TooManyRequests
    is InternalServerException -> HttpStatusCode.InternalServerError
}
