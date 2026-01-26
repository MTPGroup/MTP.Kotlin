package tech.hanasaki.azusa.common.platform.api

import io.ktor.http.HttpStatusCode

class ApiException(
    val status: HttpStatusCode,
    val code: String,
    message: String,
    val detail: String? = null,
) : RuntimeException(message)