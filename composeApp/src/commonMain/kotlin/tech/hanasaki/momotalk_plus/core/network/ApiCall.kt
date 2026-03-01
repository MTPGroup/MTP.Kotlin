package tech.hanasaki.momotalk_plus.core.network

suspend inline fun <T> callApi(
    errorMapper: NetworkErrorMapper,
    crossinline block: suspend () -> ApiEnvelope<T>,
): AppResult<T> {
    return try {
        val envelope = block()
        if (!envelope.success) {
            val error = if (!envelope.errors.isNullOrEmpty()) {
                AppError.Validation(
                    message = envelope.message,
                    details = envelope.errors,
                    code = envelope.code,
                )
            } else {
                AppError.Http(
                    status = 200,
                    message = envelope.message,
                    code = envelope.code,
                    details = envelope.errors,
                )
            }
            AppResult.Failure(error)
        } else {
            val data = envelope.data
            if (data == null) {
                AppResult.Failure(
                    AppError.Serialization(message = "响应数据为空", causeMessage = "ApiEnvelope.data is null")
                )
            } else {
                AppResult.Success(data)
            }
        }
    } catch (t: Throwable) {
        AppResult.Failure(errorMapper.map(t))
    }
}

suspend inline fun <T> callRawApi(
    errorMapper: NetworkErrorMapper,
    crossinline block: suspend () -> T,
): AppResult<T> {
    return try {
        AppResult.Success(block())
    } catch (t: Throwable) {
        AppResult.Failure(errorMapper.map(t))
    }
}
