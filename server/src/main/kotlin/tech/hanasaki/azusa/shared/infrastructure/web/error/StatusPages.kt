package tech.hanasaki.azusa.shared.infrastructure.web.error

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import tech.hanasaki.azusa.shared.domain.exception.*
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
                    code = "VALIDATION_ERROR",
                    errors = exception.details
                )
            )
        }

        // 认证异常
        exception<AuthenticationException> { call, exception ->
            logger.warn { "认证失败: ${exception.message}" }
            call.respond(
                HttpStatusCode.Unauthorized,
                ApiResponse.error<Unit>(
                    message = exception.message,
                    code = "AUTHENTICATION_FAILED"
                )
            )
        }

        // 权限异常
        exception<AuthorizationException> { call, exception ->
            logger.warn { "权限不足: ${exception.message}" }
            call.respond(
                HttpStatusCode.Forbidden,
                ApiResponse.error<Unit>(
                    message = exception.message,
                    code = "AUTHORIZATION_FAILED"
                )
            )
        }

        // 冲突异常（资源已存在）
        exception<ConflictException> { call, exception ->
            logger.warn { "资源冲突: ${exception.message}" }
            call.respond(
                HttpStatusCode.Conflict,
                ApiResponse.error<Unit>(
                    message = exception.message,
                    code = "CONFLICT"
                )
            )
        }

        // 未找到异常
        exception<NotFoundException> { call, exception ->
            logger.warn { "资源未找到: ${exception.message}" }
            call.respond(
                HttpStatusCode.NotFound,
                ApiResponse.error<Unit>(
                    message = exception.message,
                    code = "NOT_FOUND"
                )
            )
        }

        // 速率限制异常
        exception<HitLimitException> { call, exception ->
            logger.warn { "请求过于频繁: ${exception.message}" }
            call.respond(
                HttpStatusCode.TooManyRequests,
                ApiResponse.error<Unit>(
                    message = exception.message,
                    code = "HIT_LIMIT"
                )
            )
        }


        // 所有 Domain 异常的通用处理
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

        // 数据库并发冲突（序列化失败）
        exception<org.jetbrains.exposed.v1.exceptions.ExposedSQLException> { call, exception ->
            if (exception.message?.contains("could not serialize access") == true) {
                logger.warn { "数据库并发冲突: ${exception.message}" }
                call.respond(
                    HttpStatusCode.Conflict,
                    ApiResponse.error<Unit>(
                        message = "操作冲突，请重试",
                        code = "CONCURRENT_CONFLICT"
                    )
                )
            } else {
                logger.error(exception) { "数据库错误" }
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse.error<Unit>(
                        message = "服务器内部错误",
                        code = "INTERNAL_SERVER_ERROR"
                    )
                )
            }
        }

        // 客户端断开连接（协程取消）
        exception<kotlinx.coroutines.CancellationException> { _, exception ->
            logger.debug { "请求已取消: ${exception.message}" }
            throw exception
        }

        // 其他所有异常
        exception<Throwable> { call, exception ->
            logger.error(exception) { "未知错误" }
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse.error<Unit>(
                    message = exception.message ?: "服务器内部错误",
                    code = "INTERNAL_SERVER_ERROR"
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
    else -> HttpStatusCode.InternalServerError
}
