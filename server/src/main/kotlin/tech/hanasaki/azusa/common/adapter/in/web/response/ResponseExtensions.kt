package tech.hanasaki.azusa.common.adapter.`in`.web.response

import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.time.Clock

/**
 * 返回成功响应，自动包装为 ApiResponse
 */
suspend inline fun <reified T : Any> RoutingCall.respondOk(data: T, message: String = "OK") {
    respond(
        ApiResponse(
            success = true,
            message = message,
            data = data,
            timestamp = Clock.System.now(),
        )
    )
}

/**
 * 返回成功响应（无数据）
 */
suspend fun RoutingCall.respondOk(message: String = "OK") {
    respond(
        ApiResponse<Nothing>(
            success = true,
            message = message,
            timestamp = Clock.System.now(),
        )
    )
}
