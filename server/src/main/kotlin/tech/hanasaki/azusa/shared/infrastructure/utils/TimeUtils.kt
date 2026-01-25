package tech.hanasaki.azusa.shared.infrastructure.utils

import io.ktor.http.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import tech.hanasaki.azusa.shared.api.response.ApiException
import kotlin.time.Instant


fun parseBeforeTimestamp(value: String): LocalDateTime {
    return runCatching { Instant.parse(value).toLocalDateTime(TimeZone.of("+08:00")) }.getOrElse {
        throw ApiException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "Invalid timestamp: before")
    }
}

fun Instant.toUtc8String(): String =
    this.toLocalDateTime(TimeZone.of("CTT")).toString()
