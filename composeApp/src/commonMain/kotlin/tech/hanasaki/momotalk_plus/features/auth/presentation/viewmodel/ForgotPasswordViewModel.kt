package tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.BaseViewModel
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.features.auth.domain.usecase.ResetPasswordUseCase
import tech.hanasaki.momotalk_plus.features.auth.domain.usecase.SendPasswordResetEmailUseCase
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.ForgotPasswordIntent
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.ForgotPasswordSideEffect
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.ForgotPasswordState

class ForgotPasswordViewModel(
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
) : BaseViewModel<ForgotPasswordState, ForgotPasswordSideEffect, ForgotPasswordIntent>(ForgotPasswordState()) {

    override fun processIntent(intent: ForgotPasswordIntent) {
        viewModelScope.launch {
            when (intent) {
                is ForgotPasswordIntent.EmailChanged ->
                    updateState { it.copy(email = intent.email, error = null) }

                is ForgotPasswordIntent.PasswordChanged ->
                    updateState { it.copy(newPassword = intent.newPassword) }

                is ForgotPasswordIntent.VerificationCodeChanged ->
                    updateState { it.copy(otpCode = intent.code) }

                is ForgotPasswordIntent.SendVerificationCode ->
                    sendVerificationCode()

                is ForgotPasswordIntent.ResetPasswordClicked ->
                    resetPassword()
            }
        }
    }

    private suspend fun sendVerificationCode() {
        updateState { it.copy(isRequestingCode = true, error = null) }
        when (val result =
            sendPasswordResetEmailUseCase(uiState.value.email)) {
            is IResult.Success -> {
                updateState { it.copy(isRequestingCode = false) }
                sendSideEffect(ForgotPasswordSideEffect.ShowToast("验证码已发送，请检查您的邮箱。"))
            }

            is IResult.Error -> {
                val errorMessage = result.error.message
                updateState { it.copy(isRequestingCode = false, error = errorMessage) }
                sendSideEffect(ForgotPasswordSideEffect.ShowToast(errorMessage))
            }
        }
    }

    private suspend fun resetPassword() {
        val currentState = uiState.value
        if (currentState.newPassword.length < 8) {
            updateState { it.copy(error = "密码长度至少为8位") }
            return
        }
        if (currentState.otpCode.isBlank()) {
            updateState { it.copy(error = "请输入验证码") }
            return
        }

        updateState { it.copy(isLoading = true, error = null) }
        when (val result = resetPasswordUseCase(
            email = currentState.email,
            otp = currentState.otpCode,
            newPassword = currentState.newPassword,
        )) {
            is IResult.Success -> {
                updateState { it.copy(isLoading = false) }
                sendSideEffect(ForgotPasswordSideEffect.NavigateToSuccess)
            }

            is IResult.Error -> {
                val errorMessage = result.error.message
                updateState { it.copy(isLoading = false, error = errorMessage) }
                sendSideEffect(ForgotPasswordSideEffect.ShowToast(errorMessage))
            }
        }
    }
}