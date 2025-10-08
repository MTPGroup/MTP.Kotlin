package tech.hanasaki.momotalk_plus.core.domain.model

data class AppError(
    val message: String,
    val code: String? = null,
)