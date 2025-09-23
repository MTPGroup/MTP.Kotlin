package tech.hanasaki.momotalk_plus.features.contacts.domain.model

sealed class ContactError {
    data object NetworkError : ContactError()
    data class ApiError(val code: Int, val message: String) : ContactError()
}