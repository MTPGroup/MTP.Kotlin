package tech.hanasaki.momotalk_plus.core.network

class AppErrorException(
    val appError: AppError,
) : IllegalStateException(appError.toDisplayMessage())

fun AppError.toDisplayMessage(): String = when (this) {
    is AppError.Validation -> {
        val detailsText = details
            ?.entries
            ?.joinToString("；") { (field, message) -> "$field: $message" }
            ?.takeIf { it.isNotBlank() }

        if (detailsText != null) "$message（$detailsText）" else message
    }

    is AppError.Http -> {
        val detailsText = details
            ?.entries
            ?.joinToString("；") { (field, message) -> "$field: $message" }
            ?.takeIf { it.isNotBlank() }

        if (detailsText != null) "$message（$detailsText）" else message
    }

    else -> message
}
