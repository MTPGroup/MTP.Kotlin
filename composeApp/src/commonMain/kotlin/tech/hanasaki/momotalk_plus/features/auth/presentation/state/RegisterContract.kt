package tech.hanasaki.momotalk_plus.features.auth.presentation.state

import tech.hanasaki.momotalk_plus.features.auth.presentation.support.AuthUiText

enum class RegisterStep {
    USER_INFO,
    VERIFY_EMAIL,
    SUCCESS,
}

data class RegisterState(
    val email: String = "",
    val otpCode: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isEmailValid: Boolean = false,
    val resendCooldownSeconds: Int = 0,
    val emailError: AuthUiText? = null,
    val otpError: AuthUiText? = null,
    val passwordError: AuthUiText? = null,
    val confirmPasswordError: AuthUiText? = null,
    val currentStep: RegisterStep = RegisterStep.USER_INFO,
)

sealed class RegisterIntent {
    data class EmailChanged(val email: String) : RegisterIntent()
    data class OTPCodeChanged(val otpCode: String) : RegisterIntent()
    data class PasswordChanged(val password: String) : RegisterIntent()
    data class ConfirmPasswordChanged(val confirmPassword: String) : RegisterIntent()
    data class InitializePendingVerification(val email: String) : RegisterIntent()

    data object ResendOTPCodeClicked : RegisterIntent()
    data object VerifyEmailClicked : RegisterIntent()
    data object RegisterClicked : RegisterIntent()
}

sealed class RegisterSideEffect {
    data object NavigateToLogin : RegisterSideEffect()
    data class ShowToast(val message: AuthUiText) : RegisterSideEffect()
}
