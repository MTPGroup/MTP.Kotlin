package tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.OtpType
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
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
) : ViewModel(), ContainerHost<RegisterState, RegisterSideEffect> {

    override val container: Container<RegisterState, RegisterSideEffect> =
        viewModelScope.container(RegisterState())

    fun onIntent(intent: RegisterIntent) {
        when (intent) {
            is RegisterIntent.EmailChanged -> intent { reduce { state.copy(email = intent.email) } }
            is RegisterIntent.OTPCodeChanged -> intent { reduce { state.copy(otpCode = intent.otpCode) } }
            is RegisterIntent.PasswordChanged -> intent { reduce { state.copy(password = intent.password) } }
            is RegisterIntent.ConfirmPasswordChanged -> intent { reduce { state.copy(confirmPassword = intent.confirmPassword) } }
            is RegisterIntent.ResendOTPCodeClicked -> sendOTPCode()
            is RegisterIntent.VerifyEmailClicked -> verifyEmail()
            is RegisterIntent.RegisterClicked -> registerUser()
        }
    }

    private fun sendOTPCode() = intent {
        val email = state.email
        reduce { state.copy(isLoading = true, error = null) }
        sendOTPCodeUseCase(email, OtpType.Email.SIGNUP)
            .onSuccess {
                reduce { state.copy(isLoading = false, isEmailValid = true, error = null) }
                postSideEffect(RegisterSideEffect.ShowToast("验证码已发送到 $email"))
            }
            .onFailure { e ->
                val errorMessage = e.message
                reduce { state.copy(isLoading = false, error = errorMessage) }
                postSideEffect(RegisterSideEffect.ShowToast("验证码发送失败: $errorMessage"))
            }
    }

    private fun verifyEmail() = intent {
        val email = state.email
        val code = state.otpCode
        reduce { state.copy(isLoading = true, error = null) }
        verifyEmailUseCase(OtpType.Email.SIGNUP, email, code)
            .onSuccess {
                reduce { state.copy(isLoading = false) }
                postSideEffect(RegisterSideEffect.NavigateToSuccessStep)
            }
            .onFailure { e ->
                val errorMessage = e.message
                reduce { state.copy(isLoading = false, error = errorMessage) }
                postSideEffect(RegisterSideEffect.ShowToast("验证失败: $errorMessage"))
            }
    }

    private fun registerUser() = intent {
        val current = state
        if (current.password != current.confirmPassword) {
            reduce { state.copy(error = "两次输入的密码不一致。") }
            return@intent
        }

        val email = if (isValidEmail(current.email)) current.email else ""
        reduce { state.copy(isLoading = true, error = null) }

        registerUserUseCase(email = email, password = current.password)
            .onSuccess {
                sendOTPCodeUseCase(email, OtpType.Email.SIGNUP)
                    .onSuccess {
                        reduce { state.copy(isEmailValid = true, error = null) }
                        postSideEffect(RegisterSideEffect.ShowToast("验证码已发送到 $email"))
                    }
                    .onFailure { e ->
                        val errorMessage = e.message
                        reduce { state.copy(error = errorMessage) }
                        postSideEffect(RegisterSideEffect.ShowToast("验证码发送失败: $errorMessage"))
                    }
                reduce { state.copy(isLoading = false) }
                postSideEffect(RegisterSideEffect.NavigateToNextStep)
            }
            .onFailure { e ->
                val errorMessage = e.message
                reduce { state.copy(isLoading = false, error = errorMessage) }
                postSideEffect(RegisterSideEffect.ShowToast("注册失败: $errorMessage"))
            }
    }
}
