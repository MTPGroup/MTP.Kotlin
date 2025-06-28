package tech.hanasaki.momotalk_plus.core.domain.model

sealed class UserError {
    data class ApiError(val code: Int, val message: String) : UserError()
    data class NetworkError(val originalException: Exception) : UserError()
    data object Unknown : UserError()
}
