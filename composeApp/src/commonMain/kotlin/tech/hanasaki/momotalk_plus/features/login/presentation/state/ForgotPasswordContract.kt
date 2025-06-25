package tech.hanasaki.momotalk_plus.features.login.presentation.state

data class ForgotPasswordState(
    val email: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val emailSent: Boolean = false
)

sealed class ForgotPasswordIntent {
    data class EmailChanged(val email: String) : ForgotPasswordIntent()
    data object SendResetLink : ForgotPasswordIntent()
}

sealed class ForgotPasswordSideEffect {
    data class ShowToast(val message: String) : ForgotPasswordSideEffect()
}