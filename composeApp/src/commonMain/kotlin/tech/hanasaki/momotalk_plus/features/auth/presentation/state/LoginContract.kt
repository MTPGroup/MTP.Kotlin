package tech.hanasaki.momotalk_plus.features.auth.presentation.state

import tech.hanasaki.momotalk_plus.features.auth.presentation.support.AuthUiText

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val emailError: AuthUiText? = null,
    val passwordError: AuthUiText? = null,
    val formError: AuthUiText? = null,

    val showEmailVerificationDialog: Boolean = false,
    val verificationEmail: String = "",
    val verificationEmailError: AuthUiText? = null,
    val verificationCode: String = "",
    val verificationCodeError: AuthUiText? = null,
    val isSendingVerificationCode: Boolean = false,
    val isVerifyingEmail: Boolean = false,
    val resendCooldownSeconds: Int = 0,
)

sealed class LoginIntent {
    data class EmailChanged(val email: String) : LoginIntent()
    data class PasswordChanged(val password: String) : LoginIntent()

    data class VerificationEmailChanged(val email: String) : LoginIntent()
    data class VerificationCodeChanged(val code: String) : LoginIntent()

    data object ForgotPasswordClicked : LoginIntent()
    data object RegisterClicked : LoginIntent()
    data object LoginClicked : LoginIntent()
    data object ResendVerificationCodeClicked : LoginIntent()
    data object VerifyEmailClicked : LoginIntent()
    data object DismissEmailVerificationDialog : LoginIntent()
}

sealed class LoginSideEffect {
    data object NavigateToRegister : LoginSideEffect()
    data object NavigateToForgotPassword : LoginSideEffect()
    data class ShowToast(val message: AuthUiText) : LoginSideEffect()
}
