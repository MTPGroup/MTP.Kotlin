package tech.hanasaki.momotalk_plus.core.common

data class AppError(
    val message: String,
    val code: String? = null
)
