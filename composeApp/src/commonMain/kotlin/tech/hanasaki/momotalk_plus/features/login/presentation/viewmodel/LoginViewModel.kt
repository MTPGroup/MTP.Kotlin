package tech.hanasaki.momotalk_plus.features.login.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.features.login.domain.usecase.LoginUserUseCase
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.core.domain.usecase.SaveLoginStateUseCase
import tech.hanasaki.momotalk_plus.features.login.presentation.state.LoginIntent
import tech.hanasaki.momotalk_plus.features.login.presentation.state.LoginSideEffect
import tech.hanasaki.momotalk_plus.features.login.presentation.state.LoginState

class LoginViewModel(
    private val loginUserUseCase: LoginUserUseCase,
    private val saveLoginStateUseCase: SaveLoginStateUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<LoginSideEffect>()
    val sideEffect: Flow<LoginSideEffect> = _sideEffect.receiveAsFlow()

    fun processIntent(intent: LoginIntent) {
        viewModelScope.launch {
            when (intent) {
                is LoginIntent.EmailChanged ->
                    _uiState.update { it.copy(email = intent.email) }

                is LoginIntent.ErrorDismissed ->
                    _uiState.update { it.copy(loginError = null) }

                is LoginIntent.LoginClicked ->
                    loginUser()

                is LoginIntent.PasswordChanged ->
                    _uiState.update { it.copy(password = intent.password) }

                is LoginIntent.ForgotPasswordClicked ->
                    _sideEffect.send(LoginSideEffect.NavigateToForgotPassword)

                is LoginIntent.RegisterClicked ->
                    _sideEffect.send(LoginSideEffect.NavigateToRegister)
            }
        }
    }

    private suspend fun loginUser() {
        _uiState.update { it.copy(isLoading = true, loginError = null) }
        val currentState = _uiState.value

        when (val loginResult = loginUserUseCase(currentState.email, currentState.password)) {
            is Result.Success -> {
                val refreshInfo = loginResult.data
                saveLoginStateUseCase(
                    uid = refreshInfo.uid,
                    idToken = refreshInfo.idToken,
                    refreshToken = refreshInfo.refreshToken,
                    expiresIn = refreshInfo.expiresIn,
                )
                _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                _sideEffect.send(LoginSideEffect.NavigateToHome(refreshInfo.uid))
            }

            is Result.Error -> {
                val errorMessage = loginResult.error.message
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loginError = errorMessage
                    )
                }
                _sideEffect.send(LoginSideEffect.ShowToast(errorMessage))
            }
        }
    }
}