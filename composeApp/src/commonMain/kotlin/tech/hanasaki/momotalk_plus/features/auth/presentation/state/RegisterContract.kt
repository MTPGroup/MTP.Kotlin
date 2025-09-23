package tech.hanasaki.momotalk_plus.features.auth.presentation.state

data class RegisterState(
    val email: String = "",
    val otpCode: String = "",
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isEmailValid: Boolean = false,
    val error: String? = null,
)

sealed class RegisterIntent {
    data class EmailChanged(val email: String) : RegisterIntent()
    data class OTPCodeChanged(val otpCode: String) : RegisterIntent()
    data class UsernameChanged(val username: String) : RegisterIntent()
    data class PasswordChanged(val password: String) : RegisterIntent()
    data class ConfirmPasswordChanged(val confirmPassword: String) : RegisterIntent()

    data object ResendOTPCodeClicked : RegisterIntent()
    data object VerifyEmailClicked : RegisterIntent()
    data object RegisterClicked : RegisterIntent()
}

sealed class RegisterSideEffect {
    data object NavigateToLogin : RegisterSideEffect()
    data object NavigateToNextStep : RegisterSideEffect()
    data object NavigateToSuccessStep : RegisterSideEffect()
    data class ShowToast(val message: String) : RegisterSideEffect()
}