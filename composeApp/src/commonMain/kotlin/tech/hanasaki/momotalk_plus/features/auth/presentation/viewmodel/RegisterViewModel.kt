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
import tech.hanasaki.momotalk_plus.features.auth.domain.model.OTPType
import tech.hanasaki.momotalk_plus.features.auth.domain.usecase.SendEmailVerificationUseCase
import tech.hanasaki.momotalk_plus.features.auth.domain.usecase.SignUpUserUseCase
import tech.hanasaki.momotalk_plus.features.auth.domain.usecase.VerifyEmailUseCase
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.RegisterIntent
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.RegisterSideEffect
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.RegisterState
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.RegisterStep
import tech.hanasaki.momotalk_plus.features.auth.presentation.support.AuthErrorCodes
import tech.hanasaki.momotalk_plus.features.auth.presentation.support.AuthErrorPresenter
import tech.hanasaki.momotalk_plus.features.auth.presentation.support.AuthUiText
import tech.hanasaki.momotalk_plus.features.auth.presentation.support.AuthValidationMessages
import tech.hanasaki.momotalk_plus.features.auth.presentation.support.appErrorCodeOrNull
import tech.hanasaki.momotalk_plus.features.auth.presentation.support.retryAfterSecondsOrNull

class RegisterViewModel(
    private val registerUserUseCase: SignUpUserUseCase,
    private val sendOtpCodeUseCase: SendEmailVerificationUseCase,
    private val verifyEmailUseCase: VerifyEmailUseCase,
) : ViewModel(), ContainerHost<RegisterState, RegisterSideEffect> {

    override val container: Container<RegisterState, RegisterSideEffect> =
        viewModelScope.container(RegisterState())

    private var cooldownJob: Job? = null

    override fun onCleared() {
        cooldownJob?.cancel()
        super.onCleared()
    }

    fun onIntent(uiIntent: RegisterIntent) {
        when (uiIntent) {
            is RegisterIntent.EmailChanged -> intent {
                reduce {
                    state.copy(
                        email = uiIntent.email,
                        emailError = null,
                    )
                }
            }

            is RegisterIntent.OTPCodeChanged -> intent {
                reduce {
                    state.copy(
                        otpCode = uiIntent.otpCode,
                        otpError = null,
                    )
                }
            }

            is RegisterIntent.PasswordChanged -> intent {
                reduce {
                    state.copy(
                        password = uiIntent.password,
                        passwordError = null,
                    )
                }
            }

            is RegisterIntent.ConfirmPasswordChanged -> intent {
                reduce {
                    state.copy(
                        confirmPassword = uiIntent.confirmPassword,
                        confirmPasswordError = null,
                    )
                }
            }

            is RegisterIntent.InitializePendingVerification -> initializePendingVerification(uiIntent.email)
            is RegisterIntent.ResendOTPCodeClicked -> sendOtpCode()
            is RegisterIntent.VerifyEmailClicked -> verifyEmail()
            is RegisterIntent.RegisterClicked -> registerUser()
        }
    }

    private fun initializePendingVerification(email: String) = intent {
        val normalizedEmail = email.trim()
        if (!isValidEmail(normalizedEmail)) {
            reduce { state.copy(emailError = AuthUiText.Resource(AuthValidationMessages.EMAIL_INVALID)) }
            return@intent
        }

        reduce {
            state.copy(
                email = normalizedEmail,
                currentStep = RegisterStep.VERIFY_EMAIL,
                emailError = null,
                otpError = null,
                passwordError = null,
                confirmPasswordError = null,
            )
        }

        sendOtpCode(normalizedEmail)
    }

    private fun sendOtpCode(emailOverride: String? = null) = intent {
        val email = (emailOverride ?: state.email).trim()
        if (!isValidEmail(email)) {
            reduce { state.copy(emailError = AuthUiText.Resource(AuthValidationMessages.EMAIL_INVALID)) }
            return@intent
        }
        if (state.resendCooldownSeconds > 0) return@intent

        reduce {
            state.copy(
                isLoading = true,
                emailError = null,
            )
        }

        sendOtpCodeUseCase(email, OTPType.VERIFY_EMAIL)
            .onSuccess {
                reduce { state.copy(isLoading = false) }
                startCooldown(60)
                postSideEffect(
                    RegisterSideEffect.ShowToast(
                        AuthUiText.Resource(
                            AuthValidationMessages.OTP_SENT_TO_EMAIL,
                            listOf(email),
                        ),
                    ),
                )
            }
            .onFailure { e ->
                val retryAfter = e.retryAfterSecondsOrNull()
                if (retryAfter != null) startCooldown(retryAfter)
                val errorMessage = AuthErrorPresenter.resolveMessage(e, AuthValidationMessages.SEND_OTP_FAILED)
                reduce { state.copy(isLoading = false) }
                postSideEffect(RegisterSideEffect.ShowToast(errorMessage))
            }
    }

    private fun verifyEmail() = intent {
        val email = state.email.trim()
        val code = state.otpCode.trim()

        if (!isValidEmail(email)) {
            reduce { state.copy(emailError = AuthUiText.Resource(AuthValidationMessages.EMAIL_INVALID)) }
            return@intent
        }
        if (code.isBlank()) {
            reduce { state.copy(otpError = AuthUiText.Resource(AuthValidationMessages.OTP_REQUIRED)) }
            return@intent
        }

        reduce {
            state.copy(
                isLoading = true,
                emailError = null,
                otpError = null,
            )
        }

        verifyEmailUseCase(OTPType.VERIFY_EMAIL, email, code)
            .onSuccess {
                reduce {
                    state.copy(
                        isLoading = false,
                        isEmailValid = true,
                        currentStep = RegisterStep.SUCCESS,
                    )
                }
            }
            .onFailure { e ->
                val errorMessage = AuthErrorPresenter.resolveMessage(e, AuthValidationMessages.VERIFY_FAILED)
                reduce { state.copy(isLoading = false) }
                postSideEffect(RegisterSideEffect.ShowToast(errorMessage))
            }
    }

    private fun registerUser() = intent {
        val email = state.email.trim()
        val password = state.password
        val confirmPassword = state.confirmPassword

        when {
            !isValidEmail(email) -> {
                reduce { state.copy(emailError = AuthUiText.Resource(AuthValidationMessages.EMAIL_INVALID)) }
                return@intent
            }

            password.length < 8 -> {
                reduce { state.copy(passwordError = AuthUiText.Resource(AuthValidationMessages.PASSWORD_MIN_LENGTH)) }
                return@intent
            }

            password != confirmPassword -> {
                reduce { state.copy(confirmPasswordError = AuthUiText.Resource(AuthValidationMessages.PASSWORD_NOT_MATCH)) }
                return@intent
            }
        }

        reduce {
            state.copy(
                isLoading = true,
                emailError = null,
                passwordError = null,
                confirmPasswordError = null,
            )
        }

        registerUserUseCase(email = email, password = password)
            .onSuccess {
                reduce {
                    state.copy(
                        isLoading = false,
                        currentStep = RegisterStep.VERIFY_EMAIL,
                    )
                }
                sendOtpCode()
            }
            .onFailure { e ->
                reduce { state.copy(isLoading = false) }

                if (e.appErrorCodeOrNull() == AuthErrorCodes.EMAIL_NOT_VERIFIED) {
                    reduce {
                        state.copy(
                            currentStep = RegisterStep.VERIFY_EMAIL,
                            emailError = null,
                            passwordError = null,
                            confirmPasswordError = null,
                        )
                    }
                    sendOtpCode(email)
                } else {
                    val errorMessage = AuthErrorPresenter.resolveMessage(e, AuthValidationMessages.SIGN_UP_FAILED)
                    postSideEffect(RegisterSideEffect.ShowToast(errorMessage))
                }
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
