package tech.hanasaki.azusa.common.adapter.`in`.web.error

import io.ktor.http.*

class ApiException(
    val status: HttpStatusCode,
    val code: String,
    message: String,
    val detail: String? = null,
) : RuntimeException(message)