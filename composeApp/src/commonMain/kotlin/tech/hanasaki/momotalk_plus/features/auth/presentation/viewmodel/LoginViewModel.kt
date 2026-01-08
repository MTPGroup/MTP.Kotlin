package tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import tech.hanasaki.momotalk_plus.features.auth.domain.usecase.SignInUserUseCase
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.LoginIntent
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.LoginSideEffect
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.LoginState

class LoginViewModel(
    private val loginUserUseCase: SignInUserUseCase,
) : ViewModel(), ContainerHost<LoginState, LoginSideEffect> {

    override val container: Container<LoginState, LoginSideEffect> =
        viewModelScope.container(LoginState())

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.EmailChanged -> intent { reduce { state.copy(email = intent.email) } }
            is LoginIntent.PasswordChanged -> intent { reduce { state.copy(password = intent.password) } }
            is LoginIntent.ErrorDismissed -> intent { reduce { state.copy(loginError = null) } }
            is LoginIntent.LoginClicked -> loginUser()
            is LoginIntent.ForgotPasswordClicked -> intent { postSideEffect(LoginSideEffect.NavigateToForgotPassword) }
            is LoginIntent.RegisterClicked -> intent { postSideEffect(LoginSideEffect.NavigateToRegister) }
        }
    }

    private fun loginUser() = intent {
        reduce { state.copy(isLoading = true, loginError = null) }
        val email = state.email
        val password = state.password
        loginUserUseCase(email, password)
            .onSuccess {
                reduce { state.copy(isLoading = false, isLoggedIn = true) }
            }
            .onFailure { error ->
                val msg = error.message
                reduce { state.copy(isLoading = false, loginError = msg) }
                postSideEffect(LoginSideEffect.ShowToast("登录失败: $msg"))
            }
    }
}
