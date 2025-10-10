package tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.BaseViewModel
import tech.hanasaki.momotalk_plus.features.auth.domain.usecase.SignInUserUseCase
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.LoginIntent
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.LoginSideEffect
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.LoginState

class LoginViewModel(
    private val loginUserUseCase: SignInUserUseCase,
) : BaseViewModel<LoginState, LoginSideEffect, LoginIntent>(LoginState()) {

    override fun processIntent(intent: LoginIntent) {
        viewModelScope.launch {
            when (intent) {
                is LoginIntent.EmailChanged ->
                    updateState { it.copy(email = intent.email) }

                is LoginIntent.PasswordChanged ->
                    updateState { it.copy(password = intent.password) }

                is LoginIntent.ErrorDismissed ->
                    updateState { it.copy(loginError = null) }

                is LoginIntent.LoginClicked ->
                    loginUser()

                is LoginIntent.ForgotPasswordClicked ->
                    sendSideEffect(LoginSideEffect.NavigateToForgotPassword)

                is LoginIntent.RegisterClicked ->
                    sendSideEffect(LoginSideEffect.NavigateToRegister)
            }
        }
    }

    private suspend fun loginUser() {
        updateState { it.copy(isLoading = true, loginError = null) }
        val currentState = getState()

        loginUserUseCase(
            currentState.email,
            currentState.password
        ).onSuccess {
            updateState { it.copy(isLoading = false, isLoggedIn = true) }
            sendSideEffect(LoginSideEffect.NavigateToHome)
        }.onFailure { error ->
            val errorMessage = error.message
            updateState {
                it.copy(
                    isLoading = false,
                    loginError = errorMessage
                )
            }
            sendSideEffect(LoginSideEffect.ShowToast("登录失败: $errorMessage"))
        }
    }
}