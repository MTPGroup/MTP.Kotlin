package tech.hanasaki.azusa.common

import org.springframework.http.HttpStatus

class ApiException(
    val status: HttpStatus,
    val code: String,
    message: String,
    val detail: String? = null,
) : RuntimeException(message)