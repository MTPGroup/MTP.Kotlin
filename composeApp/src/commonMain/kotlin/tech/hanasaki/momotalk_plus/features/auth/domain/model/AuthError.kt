package tech.hanasaki.momotalk_plus.features.auth.domain.model

sealed class AuthError {
    data class ApiError(val code: Int, val message: String) : AuthError()
    data class NetworkError(val originalException: Exception) : AuthError()
    data object Unknown : AuthError()
}
