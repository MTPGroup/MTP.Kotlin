package tech.hanasaki.azusa.shared.infrastructure.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
import tech.hanasaki.azusa.shared.ApiResponse
import tech.hanasaki.azusa.shared.ErrorResponse
import kotlin.time.Clock

@RestControllerAdvice
class ApiResponseBodyAdvice(
    private val objectMapper: ObjectMapper,
) : ResponseBodyAdvice<Any> {
    override fun supports(
        returnType: MethodParameter,
        converterType: Class<out HttpMessageConverter<*>>,
    ): Boolean = true

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse,
    ): Any? {
        if (body == null || body is ApiResponse<*> || body is ErrorResponse) {
            return body
        }
        val payload = ApiResponse(
            success = true,
            message = "OK",
            data = body,
            timestamp = Clock.System.now().toString(),
        )
        return if (body is String) {
            objectMapper.writeValueAsString(payload)
        } else {
            payload
        }
    }
}
