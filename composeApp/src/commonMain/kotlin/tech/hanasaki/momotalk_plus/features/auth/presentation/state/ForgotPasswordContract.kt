package tech.hanasaki.momotalk_plus.features.auth.presentation.state

data class ForgotPasswordState(
    val email: String = "",
    val newPassword: String = "",
    val otpCode: String = "",
    val isLoading: Boolean = false,
    val isRequestingCode: Boolean = false,
    val error: String? = null,
)

sealed class ForgotPasswordIntent {
    data class EmailChanged(val email: String) : ForgotPasswordIntent()
    data class PasswordChanged(val newPassword: String) : ForgotPasswordIntent()
    data class VerificationCodeChanged(val code: String) : ForgotPasswordIntent()
    data object SendVerificationCode : ForgotPasswordIntent()
    data object ResetPasswordClicked : ForgotPasswordIntent()
}

sealed class ForgotPasswordSideEffect {
    data object NavigateToSuccess : ForgotPasswordSideEffect()
    data class ShowToast(val message: String) : ForgotPasswordSideEffect()
}