package tech.hanasaki.momotalk_plus.features.login.presentation.state

data class RegisterState(
    val binding: String = "",
    val username: String = "",
    val password: String = "",
    val verificationId: String = "",
    val verificationCode: String = "",
    val verificationToken: String = "",
    val confirmPassword: String = "",
    val isRequestingCode: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val captchaImage: String? = null,
    val captchaToken: String = "",
    val captchaInput: String = "",
    val showCaptchaDialog: Boolean = false
)

sealed class RegisterIntent {
    data class BindingChanged(val email: String) : RegisterIntent()
    data class UsernameChanged(val username: String) : RegisterIntent()
    data class PasswordChanged(val password: String) : RegisterIntent()
    data class VerificationCodeChanged(val token: String) : RegisterIntent()
    data class ConfirmPasswordChanged(val confirmPassword: String) : RegisterIntent()
    data class CaptchaInputChanged(val input: String) : RegisterIntent()
    data object RequestVerificationCode : RegisterIntent()
    data object VerifyCodeAndProceed : RegisterIntent()
    data object RequestCaptcha : RegisterIntent()
    data object SubmitCaptcha : RegisterIntent()
    data object DismissCaptchaDialog : RegisterIntent()
    data object RegisterClicked : RegisterIntent()
}

sealed class RegisterSideEffect {
    data object NavigateToLogin : RegisterSideEffect()
    data object NavigateToNextStep : RegisterSideEffect()
    data object NavigateToSuccessStep : RegisterSideEffect()
    data class ShowToast(val message: String) : RegisterSideEffect()
}