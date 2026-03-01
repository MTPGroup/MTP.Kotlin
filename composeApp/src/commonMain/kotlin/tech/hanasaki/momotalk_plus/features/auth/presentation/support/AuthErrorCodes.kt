package tech.hanasaki.momotalk_plus.features.auth.presentation.support

import tech.hanasaki.momotalk_plus.core.network.AppError
import tech.hanasaki.momotalk_plus.core.network.AppErrorException

object AuthErrorCodes {
    const val EMAIL_NOT_VERIFIED = "EMAIL_NOT_VERIFIED"
}

fun Throwable.appErrorOrNull(): AppError? = (this as? AppErrorException)?.appError

fun Throwable.appErrorCodeOrNull(): String? {
    val appError = appErrorOrNull() ?: return null
    return when (appError) {
        is AppError.Validation -> appError.code
        is AppError.Unauthorized -> appError.code
        is AppError.Forbidden -> appError.code
        is AppError.NotFound -> appError.code
        is AppError.Conflict -> appError.code
        is AppError.RateLimited -> appError.code
        is AppError.Http -> appError.code
        is AppError.Network -> null
        is AppError.Serialization -> null
        is AppError.Unknown -> null
    }
}

fun Throwable.retryAfterSecondsOrNull(): Int? {
    val retryAfterRaw = (appErrorOrNull() as? AppError.RateLimited)?.retryAfter ?: return null
    val seconds = if (retryAfterRaw > 600) retryAfterRaw / 1000 else retryAfterRaw
    return seconds.toInt().coerceAtLeast(1)
}
