package tech.hanasaki.momotalk_plus.core.utils

import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun formatTimestamp(timestamp: String): String {
    return try {
        val instant = Instant.parse(timestamp)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        when {
            dateTime.date == now.date -> {
                "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
            }

            dateTime.year == now.year -> {
                "${dateTime.month.number.toString().padStart(2, '0')}/${dateTime.day.toString().padStart(2, '0')}"
            }

            else -> {
                "${dateTime.year}/${dateTime.month.number.toString().padStart(2, '0')}/${
                    dateTime.day.toString().padStart(2, '0')
                }"
            }
        }
    } catch (e: Exception) {
        ""
    }
}
