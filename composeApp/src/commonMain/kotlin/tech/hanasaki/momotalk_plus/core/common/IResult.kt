package tech.hanasaki.momotalk_plus.core.common


sealed class IResult<out T, out E> {
    data class Success<out T>(val data: T) : IResult<T, Nothing>()
    data class Error<out E>(val error: E) : IResult<Nothing, E>()

    inline fun <R> fold(
        onSuccess: (T) -> R,
        onError: (E) -> R,
    ): R = when (this) {
        is Success -> onSuccess(data)
        is Error -> onError(error)
    }

    inline fun <R> map(transform: (T) -> R): IResult<R, E> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
        }
    }

    inline fun <R> mapError(transform: (E) -> R): IResult<T, R> {
        return when (this) {
            is Success -> this
            is Error -> Error(transform(error))
        }
    }
}