package tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.BaseViewModel
import tech.hanasaki.momotalk_plus.core.common.IResult
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

                is RegisterIntent.UsernameChanged ->
                    updateState { it.copy(username = intent.username) }

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
        when (val result = sendOTPCodeUseCase(currentState.email)) {
            is IResult.Success -> {
                updateState { it.copy(isEmailValid = true, error = null) }
                sendSideEffect(RegisterSideEffect.ShowToast("验证码已发送到 ${currentState.email}"))
            }

            is IResult.Error -> {
                val errorMessage = result.error.message
                updateState { it.copy(error = errorMessage) }
                sendSideEffect(RegisterSideEffect.ShowToast(errorMessage))
            }
        }
    }

    private suspend fun verifyEmail() {
        val currentState = getState()
        when (val result = verifyEmailUseCase(currentState.email, currentState.otpCode)) {
            is IResult.Success -> {
                sendSideEffect(RegisterSideEffect.NavigateToSuccessStep)
            }

            is IResult.Error -> {
                val errorMessage = result.error.message
                updateState { it.copy(error = errorMessage) }
                sendSideEffect(RegisterSideEffect.ShowToast(errorMessage))
            }
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

        when (val result = registerUserUseCase(
            email = email,
            username = currentState.username,
            password = currentState.password,
        )) {
            is IResult.Success -> {
                sendOTPCode()
                sendSideEffect(RegisterSideEffect.NavigateToNextStep)
            }

            is IResult.Error -> {
                val errorMessage = result.error.message
                updateState { it.copy(error = errorMessage) }
                sendSideEffect(RegisterSideEffect.ShowToast(errorMessage))
            }
        }
    }
}