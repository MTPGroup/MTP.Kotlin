package tech.hanasaki.azusa.shared.infrastructure.utils

import org.springframework.http.HttpStatus

class ApiException(
    val status: HttpStatus,
    val code: String,
    message: String,
    val detail: String? = null,
) : RuntimeException(message)
