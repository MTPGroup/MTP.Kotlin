package tech.hanasaki.momotalk_plus.features.login.presentation.state

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val loginError: String? = null,
    val isLoggedIn: Boolean = false
)

sealed class LoginIntent {
    data class EmailChanged(val email: String) : LoginIntent()
    data class PasswordChanged(val password: String) : LoginIntent()
    data object ForgotPasswordClicked : LoginIntent()
    data object RegisterClicked : LoginIntent()
    data object LoginClicked : LoginIntent()
    data object ErrorDismissed : LoginIntent()
}

sealed class LoginSideEffect {
    data class NavigateToHome(val uid: String) : LoginSideEffect()
    data object NavigateToRegister : LoginSideEffect()
    data object NavigateToForgotPassword : LoginSideEffect()
    data class ShowToast(val message: String) : LoginSideEffect()
}