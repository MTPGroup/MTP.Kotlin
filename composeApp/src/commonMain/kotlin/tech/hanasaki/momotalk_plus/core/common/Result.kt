package tech.hanasaki.momotalk_plus.core.common

import androidx.compose.ui.input.key.Key

sealed class Result<out T, out E> {
    data class Success<out T>(val data: T) : Result<T, Nothing>()
    data class Error<out E>(val error: E) : Result<Nothing, E>()

    inline fun <R> fold(
        onSuccess: (T) -> R,
        onError: (E) -> R
    ): R = when (this) {
        is Success -> onSuccess(data)
        is Error -> onError(error)
    }

    inline fun <R> map(transform: (T) -> R): Result<R, E> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
        }
    }

    inline fun <R> mapError(transform: (E) -> R): Result<T, R> {
        return when (this) {
            is Success -> this
            is Error -> Error(transform(error))
        }
    }
}