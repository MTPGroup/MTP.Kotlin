package tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.features.auth.domain.usecase.ResetPasswordUseCase
import tech.hanasaki.momotalk_plus.features.auth.domain.usecase.SendPasswordResetEmailUseCase
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.ForgotPasswordIntent
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.ForgotPasswordSideEffect
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.ForgotPasswordState

class ForgotPasswordViewModel(
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordState())
    val uiState: StateFlow<ForgotPasswordState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<ForgotPasswordSideEffect>()
    val sideEffect: Flow<ForgotPasswordSideEffect> = _sideEffect.receiveAsFlow()

    fun processIntent(intent: ForgotPasswordIntent) {
        viewModelScope.launch {
            when (intent) {
                is ForgotPasswordIntent.EmailChanged ->
                    _uiState.update { it.copy(email = intent.email, error = null) }

                is ForgotPasswordIntent.PasswordChanged ->
                    _uiState.update { it.copy(newPassword = intent.newPassword) }

                is ForgotPasswordIntent.VerificationCodeChanged ->
                    _uiState.update { it.copy(otpCode = intent.code) }

                is ForgotPasswordIntent.SendVerificationCode ->
                    sendVerificationCode()

                is ForgotPasswordIntent.ResetPasswordClicked ->
                    resetPassword()
            }
        }
    }

    private suspend fun sendVerificationCode() {
        _uiState.update { it.copy(isRequestingCode = true, error = null) }
        when (val result =
            sendPasswordResetEmailUseCase(uiState.value.email)) {
            is IResult.Success -> {
                _uiState.update { it.copy(isRequestingCode = false) }
                _sideEffect.send(ForgotPasswordSideEffect.ShowToast("验证码已发送，请检查您的邮箱。"))
            }

            is IResult.Error -> {
                val errorMessage = result.error.message
                _uiState.update { it.copy(isRequestingCode = false, error = errorMessage) }
                _sideEffect.send(ForgotPasswordSideEffect.ShowToast(errorMessage))
            }
        }
    }

    private suspend fun resetPassword() {
        val currentState = uiState.value
        if (currentState.newPassword.length < 8) {
            _uiState.update { it.copy(error = "密码长度至少为8位") }
            return
        }
        if (currentState.otpCode.isBlank()) {
            _uiState.update { it.copy(error = "请输入验证码") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        when (val result = resetPasswordUseCase(
            email = currentState.email,
            otp = currentState.otpCode,
            newPassword = currentState.newPassword,
        )) {
            is IResult.Success -> {
                _uiState.update { it.copy(isLoading = false) }
                _sideEffect.send(ForgotPasswordSideEffect.NavigateToSuccess)
            }

            is IResult.Error -> {
                val errorMessage = result.error.message
                _uiState.update { it.copy(isLoading = false, error = errorMessage) }
                _sideEffect.send(ForgotPasswordSideEffect.ShowToast(errorMessage))
            }
        }
    }
}