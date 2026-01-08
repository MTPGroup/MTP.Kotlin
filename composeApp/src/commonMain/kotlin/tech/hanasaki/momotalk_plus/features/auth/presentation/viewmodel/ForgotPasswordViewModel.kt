package tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import tech.hanasaki.momotalk_plus.features.auth.domain.usecase.ResetPasswordUseCase
import tech.hanasaki.momotalk_plus.features.auth.domain.usecase.SendEmailVerificationUseCase
import tech.hanasaki.momotalk_plus.features.auth.domain.usecase.SendPasswordResetEmailUseCase
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.ForgotPasswordIntent
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.ForgotPasswordSideEffect
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.ForgotPasswordState

class ForgotPasswordViewModel(
    private val sendEmailVerificationUseCase: SendEmailVerificationUseCase,
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
) : ViewModel(), ContainerHost<ForgotPasswordState, ForgotPasswordSideEffect> {

    override val container: Container<ForgotPasswordState, ForgotPasswordSideEffect> =
        viewModelScope.container(ForgotPasswordState())

    fun onIntent(intent: ForgotPasswordIntent) {
        when (intent) {
            is ForgotPasswordIntent.EmailChanged -> intent { reduce { state.copy(email = intent.email, error = null) } }
            is ForgotPasswordIntent.PasswordChanged -> intent { reduce { state.copy(newPassword = intent.newPassword) } }
            is ForgotPasswordIntent.VerificationCodeChanged -> intent { reduce { state.copy(otpCode = intent.code) } }
            is ForgotPasswordIntent.SendVerificationCode -> sendVerificationCode()
            is ForgotPasswordIntent.ResetPasswordClicked -> resetPassword()
        }
    }

    private fun sendVerificationCode() = intent {
        val email = state.email
        reduce { state.copy(isRequestingCode = true, error = null) }
        sendPasswordResetEmailUseCase(email = email)
            .onSuccess {
                reduce { state.copy(isRequestingCode = false) }
                postSideEffect(ForgotPasswordSideEffect.ShowToast("验证码已发送，请检查您的邮箱。"))
            }
            .onFailure { exception ->
                val errorMessage = exception.message ?: "发送验证码失败，请稍后重试。"
                reduce { state.copy(isRequestingCode = false, error = errorMessage) }
                postSideEffect(ForgotPasswordSideEffect.ShowToast(errorMessage))
            }
    }

    private fun resetPassword() = intent {
        val current = state
        if (current.newPassword.length < 8) {
            reduce { state.copy(error = "密码长度至少为8位") }
            return@intent
        }
        if (current.otpCode.isBlank()) {
            reduce { state.copy(error = "请输入验证码") }
            return@intent
        }

        reduce { state.copy(isLoading = true, error = null) }
        resetPasswordUseCase(
            email = current.email,
            otp = current.otpCode,
            newPassword = current.newPassword,
        ).onSuccess {
            reduce { state.copy(isLoading = false) }
            postSideEffect(ForgotPasswordSideEffect.NavigateToSuccess)
        }.onFailure { exception ->
            val errorMessage = exception.message ?: "重置密码失败，请稍后重试。"
            reduce { state.copy(isLoading = false, error = errorMessage) }
            postSideEffect(ForgotPasswordSideEffect.ShowToast(errorMessage))
        }
    }
}
