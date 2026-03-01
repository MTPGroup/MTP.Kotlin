package tech.hanasaki.azusa.shared.domain.exception

object ErrorCodes {
    const val BAD_REQUEST = "BAD_REQUEST"
    const val BAD_REQUEST_BODY = "BAD_REQUEST_BODY"

    const val VALIDATION_ERROR = "VALIDATION_ERROR"
    const val AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED"
    const val AUTHORIZATION_FAILED = "AUTHORIZATION_FAILED"
    const val EMAIL_NOT_VERIFIED = "EMAIL_NOT_VERIFIED"
    const val CONFLICT = "CONFLICT"
    const val NOT_FOUND = "NOT_FOUND"
    const val HIT_LIMIT = "HIT_LIMIT"

    const val CONCURRENT_CONFLICT = "CONCURRENT_CONFLICT"
    const val INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR"
}
