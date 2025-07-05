package tech.hanasaki.momotalk_plus.features.login.presentation.state

data class ForgotPasswordState(
    val email: String = "",
    val newPassword: String = "",
    val verificationId: String = "",
    val verificationCode: String = "",
    val verificationToken: String = "",
    val captchaImage: String? = null,
    val captchaToken: String = "",
    val captchaInput: String = "",
    val showCaptchaDialog: Boolean = false,
    val isLoading: Boolean = false,
    val isRequestingCode: Boolean = false,
    val error: String? = null,
)

sealed class ForgotPasswordIntent {
    data class EmailChanged(val email: String) : ForgotPasswordIntent()
    data class PasswordChanged(val newPassword: String) : ForgotPasswordIntent()
    data class VerificationCodeChanged(val code: String) : ForgotPasswordIntent()
    data class CaptchaInputChanged(val input: String) : ForgotPasswordIntent()
    data object DismissCaptchaDialog : ForgotPasswordIntent()
    data object GetCaptcha : ForgotPasswordIntent()
    data object VerifyCaptcha : ForgotPasswordIntent()
    data object VerifyCode : ForgotPasswordIntent()
    data class SendVerificationCode(val captchaToken: String) : ForgotPasswordIntent()
}

sealed class ForgotPasswordSideEffect {
    data object NavigateToSuccess : ForgotPasswordSideEffect()
    data class ShowToast(val message: String) : ForgotPasswordSideEffect()
}