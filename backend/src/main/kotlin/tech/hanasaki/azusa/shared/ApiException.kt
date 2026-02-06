package tech.hanasaki.azusa.shared

import org.springframework.http.HttpStatus

class ApiException(
    val status: HttpStatus,
    val code: String,
    message: String,
    val detail: String? = null,
) : RuntimeException(message)