package tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.core.utils.isValidEmail
import tech.hanasaki.momotalk_plus.features.auth.domain.usecase.SendEmailVerificationUseCase
import tech.hanasaki.momotalk_plus.features.auth.domain.usecase.SignUpUserUseCase
import tech.hanasaki.momotalk_plus.features.auth.domain.usecase.VerifyEmailUseCase
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.RegisterIntent
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.RegisterSideEffect
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.RegisterState


class RegisterViewModel(
    private val registerUserUseCase: SignUpUserUseCase,
    private val sendOTPCodeUseCase: SendEmailVerificationUseCase,
    private val verifyEmailUseCase: VerifyEmailUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterState())
    val uiState: StateFlow<RegisterState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<RegisterSideEffect>()
    val sideEffect: Flow<RegisterSideEffect> = _sideEffect.receiveAsFlow()

    fun processIntent(intent: RegisterIntent) {
        viewModelScope.launch {
            when (intent) {
                is RegisterIntent.EmailChanged ->
                    _uiState.update { it.copy(email = intent.email) }

                is RegisterIntent.OTPCodeChanged ->
                    _uiState.update { it.copy(otpCode = intent.otpCode) }

                is RegisterIntent.UsernameChanged ->
                    _uiState.update { it.copy(username = intent.username) }

                is RegisterIntent.PasswordChanged ->
                    _uiState.update { it.copy(password = intent.password) }

                is RegisterIntent.ConfirmPasswordChanged ->
                    _uiState.update { it.copy(confirmPassword = intent.confirmPassword) }

                is RegisterIntent.SendOTPCodeClicked ->
                    sendOTPCode()

                is RegisterIntent.VerifyEmailClicked ->
                    verifyEmail()

                is RegisterIntent.RegisterClicked ->
                    registerUser()
            }
        }
    }

    private suspend fun sendOTPCode() {
        val currentState = _uiState.value
        val isEmail = isValidEmail(currentState.email)
        if (!isEmail) {
            _uiState.update { it.copy(error = "请输入有效的邮箱地址。") }
            return
        }

        when (val result = sendOTPCodeUseCase(currentState.email)) {
            is Result.Success -> {
                _uiState.update { it.copy(isEmailValid = true, error = null) }
                _sideEffect.send(RegisterSideEffect.ShowToast("验证码已发送到 ${currentState.email}"))
            }

            is Result.Error -> {
                val errorMessage = result.error.message
                _uiState.update { it.copy(error = errorMessage) }
                _sideEffect.send(RegisterSideEffect.ShowToast(errorMessage))
            }
        }
    }

    private suspend fun verifyEmail() {
        val currentState = _uiState.value
        when (val result = verifyEmailUseCase(currentState.email, currentState.otpCode)) {
            is Result.Success -> {
                _sideEffect.send(RegisterSideEffect.NavigateToNextStep)
            }

            is Result.Error -> {
                val errorMessage = result.error.message
                _uiState.update { it.copy(error = errorMessage) }
                _sideEffect.send(RegisterSideEffect.ShowToast(errorMessage))
            }
        }
    }

    private suspend fun registerUser() {
        val currentState = _uiState.value
        if (currentState.password != currentState.confirmPassword) {
            _uiState.update { it.copy(error = "两次输入的密码不一致。") }
            return
        }

        val isEmail = isValidEmail(currentState.email)
        val email = if (isEmail) currentState.email else ""

        when (val result = registerUserUseCase(
            email = email,
            username = currentState.username,
            password = currentState.password,
        )) {
            is Result.Success -> {
                _sideEffect.send(RegisterSideEffect.NavigateToSuccessStep)
            }

            is Result.Error -> {
                val errorMessage = result.error.message
                _uiState.update { it.copy(error = errorMessage) }
                _sideEffect.send(RegisterSideEffect.ShowToast(errorMessage))
            }
        }
    }
}