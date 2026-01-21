package tech.hanasaki.azusa.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import tech.hanasaki.azusa.shared.api.ApiResponse
import kotlin.time.Clock

fun Application.configureApiResponseWrapper() {
    install(createApplicationPlugin("ApiResponseWrapper") {
        onCallRespond { call, body ->
            val status = call.response.status()
            if (status == HttpStatusCode.NoContent) {
                return@onCallRespond
            }
            if (body is ApiResponse<*>) {
                return@onCallRespond
            }
            if (body is HttpStatusCode) {
                return@onCallRespond
            }
            transformBody {
                ApiResponse(
                    success = true,
                    message = "OK",
                    data = body,
                    timestamp = Clock.System.now().toString(),
                )
            }
        }
    })
}
