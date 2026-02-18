package tech.hanasaki.azusa.shared.infrastructure.config

import io.ktor.server.config.*

fun ApplicationConfig.requireString(path: String): String {
    return property(path).getString()
}

fun ApplicationConfig.optionalString(path: String): String? =
    propertyOrNull(path)?.getString()

fun ApplicationConfig.optionalInt(path: String): Int? =
    optionalString(path)?.toIntOrNull()

fun ApplicationConfig.optionalLong(path: String): Long? =
    optionalString(path)?.toLongOrNull()

fun ApplicationConfig.optionalBoolean(path: String): Boolean? =
    optionalString(path)?.toBooleanStrictOrNull()
