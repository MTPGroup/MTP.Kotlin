package tech.hanasaki.momotalk_plus.features.login.presentation.state

data class RegisterState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class RegisterIntent {
    data class EmailChanged(val email: String) : RegisterIntent()
    data class PasswordChanged(val password: String) : RegisterIntent()
    data class ConfirmPasswordChanged(val confirmPassword: String) : RegisterIntent()
    data object RegisterClicked : RegisterIntent()
}

sealed class RegisterSideEffect {
    data object NavigateToLogin : RegisterSideEffect()
    data class ShowToast(val message: String) : RegisterSideEffect()
}