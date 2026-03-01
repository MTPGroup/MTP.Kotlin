package tech.hanasaki.momotalk_plus.features.auth.presentation.state

import tech.hanasaki.momotalk_plus.features.auth.presentation.support.AuthUiText

data class ForgotPasswordState(
    val email: String = "",
    val newPassword: String = "",
    val otpCode: String = "",
    val isLoading: Boolean = false,
    val isRequestingCode: Boolean = false,
    val resendCooldownSeconds: Int = 0,
    val emailError: AuthUiText? = null,
    val passwordError: AuthUiText? = null,
    val otpError: AuthUiText? = null,
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
    data class ShowToast(val message: AuthUiText) : ForgotPasswordSideEffect()
}
