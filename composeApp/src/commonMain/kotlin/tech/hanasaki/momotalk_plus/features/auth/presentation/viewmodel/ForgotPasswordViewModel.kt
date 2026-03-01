package tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import tech.hanasaki.momotalk_plus.core.utils.isValidEmail
import tech.hanasaki.momotalk_plus.features.auth.domain.usecase.ResetPasswordUseCase
import tech.hanasaki.momotalk_plus.features.auth.domain.usecase.SendPasswordResetEmailUseCase
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.ForgotPasswordIntent
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.ForgotPasswordSideEffect
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.ForgotPasswordState
import tech.hanasaki.momotalk_plus.features.auth.presentation.support.AuthErrorPresenter
import tech.hanasaki.momotalk_plus.features.auth.presentation.support.AuthUiText
import tech.hanasaki.momotalk_plus.features.auth.presentation.support.AuthValidationMessages
import tech.hanasaki.momotalk_plus.features.auth.presentation.support.retryAfterSecondsOrNull

class ForgotPasswordViewModel(
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
) : ViewModel(), ContainerHost<ForgotPasswordState, ForgotPasswordSideEffect> {

    override val container: Container<ForgotPasswordState, ForgotPasswordSideEffect> =
        viewModelScope.container(ForgotPasswordState())

    private var cooldownJob: Job? = null

    override fun onCleared() {
        cooldownJob?.cancel()
        super.onCleared()
    }

    fun onIntent(uiIntent: ForgotPasswordIntent) {
        when (uiIntent) {
            is ForgotPasswordIntent.EmailChanged -> intent {
                reduce {
                    state.copy(
                        email = uiIntent.email,
                        emailError = null,
                    )
                }
            }

            is ForgotPasswordIntent.PasswordChanged -> intent {
                reduce {
                    state.copy(
                        newPassword = uiIntent.newPassword,
                        passwordError = null,
                    )
                }
            }

            is ForgotPasswordIntent.VerificationCodeChanged -> intent {
                reduce {
                    state.copy(
                        otpCode = uiIntent.code,
                        otpError = null,
                    )
                }
            }

            is ForgotPasswordIntent.SendVerificationCode -> sendVerificationCode()
            is ForgotPasswordIntent.ResetPasswordClicked -> resetPassword()
        }
    }

    private fun sendVerificationCode() = intent {
        val email = state.email.trim()
        if (!isValidEmail(email)) {
            reduce { state.copy(emailError = AuthUiText.Resource(AuthValidationMessages.EMAIL_INVALID)) }
            return@intent
        }
        if (state.resendCooldownSeconds > 0) return@intent

        reduce {
            state.copy(
                isRequestingCode = true,
                emailError = null,
            )
        }

        sendPasswordResetEmailUseCase(email = email)
            .onSuccess {
                reduce { state.copy(isRequestingCode = false) }
                startCooldown(60)
                postSideEffect(
                    ForgotPasswordSideEffect.ShowToast(
                        AuthUiText.Resource(AuthValidationMessages.OTP_SENT_CHECK_EMAIL),
                    ),
                )
            }
            .onFailure { exception ->
                val retryAfter = exception.retryAfterSecondsOrNull()
                if (retryAfter != null) startCooldown(retryAfter)
                val errorMessage = AuthErrorPresenter.resolveMessage(exception, AuthValidationMessages.SEND_OTP_RETRY_FAILED)
                reduce { state.copy(isRequestingCode = false) }
                postSideEffect(ForgotPasswordSideEffect.ShowToast(errorMessage))
            }
    }

    private fun resetPassword() = intent {
        val current = state
        val email = current.email.trim()

        if (!isValidEmail(email)) {
            reduce { state.copy(emailError = AuthUiText.Resource(AuthValidationMessages.EMAIL_INVALID)) }
            return@intent
        }
        if (current.newPassword.length < 8) {
            reduce { state.copy(passwordError = AuthUiText.Resource(AuthValidationMessages.PASSWORD_MIN_LENGTH)) }
            return@intent
        }
        if (current.otpCode.isBlank()) {
            reduce { state.copy(otpError = AuthUiText.Resource(AuthValidationMessages.OTP_REQUIRED)) }
            return@intent
        }

        reduce {
            state.copy(
                isLoading = true,
                emailError = null,
                passwordError = null,
                otpError = null,
            )
        }

        resetPasswordUseCase(
            email = email,
            otp = current.otpCode,
            newPassword = current.newPassword,
        ).onSuccess {
            reduce { state.copy(isLoading = false) }
            postSideEffect(ForgotPasswordSideEffect.NavigateToSuccess)
        }.onFailure { exception ->
            val errorMessage = AuthErrorPresenter.resolveMessage(exception, AuthValidationMessages.RESET_PASSWORD_FAILED)
            reduce { state.copy(isLoading = false) }
            postSideEffect(ForgotPasswordSideEffect.ShowToast(errorMessage))
        }
    }

    private fun startCooldown(seconds: Int) {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            var remaining = seconds.coerceAtLeast(0)
            while (remaining > 0) {
                intent { reduce { state.copy(resendCooldownSeconds = remaining) } }
                delay(1000)
                remaining--
            }
            intent { reduce { state.copy(resendCooldownSeconds = 0) } }
        }
    }
}
