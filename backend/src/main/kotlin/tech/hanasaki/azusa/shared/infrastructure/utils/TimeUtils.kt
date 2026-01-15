package tech.hanasaki.azusa.shared.infrastructure.utils

import org.springframework.http.HttpStatus
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun parseBeforeTimestamp(value: String): LocalDateTime {
    return runCatching { Instant.parse(value).toLocalDateTime(TimeZone.UTC) }.getOrElse {
        throw ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Invalid timestamp: before")
    }
}
