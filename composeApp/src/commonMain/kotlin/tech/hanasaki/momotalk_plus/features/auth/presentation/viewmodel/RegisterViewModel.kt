package tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.OtpType
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.BaseViewModel
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
) : BaseViewModel<RegisterState, RegisterSideEffect, RegisterIntent>(RegisterState()) {

    override fun processIntent(intent: RegisterIntent) {
        viewModelScope.launch {
            when (intent) {
                is RegisterIntent.EmailChanged ->
                    updateState { it.copy(email = intent.email) }

                is RegisterIntent.OTPCodeChanged ->
                    updateState { it.copy(otpCode = intent.otpCode) }

                is RegisterIntent.PasswordChanged ->
                    updateState { it.copy(password = intent.password) }

                is RegisterIntent.ConfirmPasswordChanged ->
                    updateState { it.copy(confirmPassword = intent.confirmPassword) }

                is RegisterIntent.ResendOTPCodeClicked ->
                    sendOTPCode()

                is RegisterIntent.VerifyEmailClicked ->
                    verifyEmail()

                is RegisterIntent.RegisterClicked ->
                    registerUser()
            }
        }
    }

    private suspend fun sendOTPCode() {
        val currentState = getState()
        sendOTPCodeUseCase(
            currentState.email,
            OtpType.Email.SIGNUP,
        ).onSuccess {
            updateState { it.copy(isEmailValid = true, error = null) }
            sendSideEffect(RegisterSideEffect.ShowToast("验证码已发送到 ${currentState.email}"))
        }
            .onFailure { e ->
                val errorMessage = e.message
                updateState { it.copy(error = errorMessage) }
                sendSideEffect(RegisterSideEffect.ShowToast("验证码发送失败: $errorMessage"))
            }
    }

    private suspend fun verifyEmail() {
        val currentState = getState()
        verifyEmailUseCase(
            OtpType.Email.SIGNUP,
            currentState.email,
            currentState.otpCode
        ).onSuccess {
            sendSideEffect(RegisterSideEffect.NavigateToSuccessStep)
        }.onFailure { e ->
            val errorMessage = e.message
            updateState { it.copy(error = errorMessage) }
            sendSideEffect(RegisterSideEffect.ShowToast("验证失败: $errorMessage"))
        }
    }

    private suspend fun registerUser() {
        val currentState = getState()
        if (currentState.password != currentState.confirmPassword) {
            updateState { it.copy(error = "两次输入的密码不一致。") }
            return
        }

        val isEmail = isValidEmail(currentState.email)
        val email = if (isEmail) currentState.email else ""

        registerUserUseCase(
            email = email,
            password = currentState.password,
        ).onSuccess {
            sendOTPCode()
            sendSideEffect(RegisterSideEffect.NavigateToNextStep)
        }.onFailure { e ->
            val errorMessage = e.message
            updateState { it.copy(error = errorMessage) }
            sendSideEffect(RegisterSideEffect.ShowToast("注册失败: $errorMessage"))
        }
    }
}