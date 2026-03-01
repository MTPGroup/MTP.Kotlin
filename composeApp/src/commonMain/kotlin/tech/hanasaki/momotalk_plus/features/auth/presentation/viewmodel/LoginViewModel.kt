package tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import tech.hanasaki.momotalk_plus.core.utils.isValidEmail
import tech.hanasaki.momotalk_plus.features.auth.domain.model.OTPType
import tech.hanasaki.momotalk_plus.features.auth.domain.usecase.SendEmailVerificationUseCase
import tech.hanasaki.momotalk_plus.features.auth.domain.usecase.SignInUserUseCase
import tech.hanasaki.momotalk_plus.features.auth.domain.usecase.VerifyEmailUseCase
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.LoginIntent
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.LoginSideEffect
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.LoginState
import tech.hanasaki.momotalk_plus.features.auth.presentation.support.AuthErrorCodes
import tech.hanasaki.momotalk_plus.features.auth.presentation.support.AuthErrorPresenter
import tech.hanasaki.momotalk_plus.features.auth.presentation.support.AuthUiText
import tech.hanasaki.momotalk_plus.features.auth.presentation.support.AuthValidationMessages
import tech.hanasaki.momotalk_plus.features.auth.presentation.support.appErrorCodeOrNull
import tech.hanasaki.momotalk_plus.features.auth.presentation.support.retryAfterSecondsOrNull

class LoginViewModel(
    private val loginUserUseCase: SignInUserUseCase,
    private val sendEmailVerificationUseCase: SendEmailVerificationUseCase,
    private val verifyEmailUseCase: VerifyEmailUseCase,
) : ViewModel(), ContainerHost<LoginState, LoginSideEffect> {

    override val container = container<LoginState, LoginSideEffect>(LoginState())

    private var cooldownJob: Job? = null

    override fun onCleared() {
        cooldownJob?.cancel()
        super.onCleared()
    }

    fun onIntent(uiIntent: LoginIntent) {
        when (uiIntent) {
            is LoginIntent.EmailChanged -> intent {
                reduce {
                    state.copy(
                        email = uiIntent.email,
                        emailError = null,
                        formError = null,
                    )
                }
            }

            is LoginIntent.PasswordChanged -> intent {
                reduce {
                    state.copy(
                        password = uiIntent.password,
                        passwordError = null,
                        formError = null,
                    )
                }
            }

            is LoginIntent.VerificationEmailChanged -> intent {
                reduce {
                    state.copy(
                        verificationEmail = uiIntent.email,
                        verificationEmailError = null,
                    )
                }
            }

            is LoginIntent.VerificationCodeChanged -> intent {
                reduce {
                    state.copy(
                        verificationCode = uiIntent.code,
                        verificationCodeError = null,
                    )
                }
            }

            is LoginIntent.LoginClicked -> loginUser()
            is LoginIntent.ResendVerificationCodeClicked -> resendVerificationCode()
            is LoginIntent.VerifyEmailClicked -> verifyEmail()
            is LoginIntent.DismissEmailVerificationDialog -> dismissEmailVerificationDialog()

            is LoginIntent.ForgotPasswordClicked -> intent { postSideEffect(LoginSideEffect.NavigateToForgotPassword) }
            is LoginIntent.RegisterClicked -> intent { postSideEffect(LoginSideEffect.NavigateToRegister) }
        }
    }

    private fun loginUser() = intent {
        val email = state.email.trim()
        val password = state.password

        when {
            email.isBlank() -> {
                reduce { state.copy(emailError = AuthUiText.Resource(AuthValidationMessages.EMAIL_REQUIRED)) }
                return@intent
            }

            !isValidEmail(email) -> {
                reduce { state.copy(emailError = AuthUiText.Resource(AuthValidationMessages.EMAIL_INVALID)) }
                return@intent
            }

            password.length < 8 -> {
                reduce { state.copy(passwordError = AuthUiText.Resource(AuthValidationMessages.PASSWORD_MIN_LENGTH)) }
                return@intent
            }
        }

        reduce {
            state.copy(
                isLoading = true,
                emailError = null,
                passwordError = null,
                formError = null,
            )
        }

        loginUserUseCase(email, password)
            .onSuccess {
                reduce { state.copy(isLoading = false) }
            }
            .onFailure { error ->
                val code = error.appErrorCodeOrNull()
                val rawMessage = error.message.orEmpty()
                val isEmailNotVerified = code == AuthErrorCodes.EMAIL_NOT_VERIFIED || rawMessage.contains("邮箱未验证")

                if (isEmailNotVerified) {
                    reduce {
                        state.copy(
                            isLoading = false,
                            showEmailVerificationDialog = true,
                            verificationEmail = email,
                            verificationEmailError = null,
                            verificationCode = "",
                            verificationCodeError = null,
                        )
                    }
                    resendVerificationCode(email)
                } else {
                    val message = AuthErrorPresenter.resolveMessage(error, AuthValidationMessages.LOGIN_FAILED)
                    reduce { state.copy(isLoading = false, formError = message) }
                    postSideEffect(LoginSideEffect.ShowToast(message))
                }
            }
    }

    private fun dismissEmailVerificationDialog() = intent {
        cooldownJob?.cancel()
        reduce {
            state.copy(
                showEmailVerificationDialog = false,
                verificationCode = "",
                verificationCodeError = null,
                verificationEmailError = null,
                isSendingVerificationCode = false,
                isVerifyingEmail = false,
                resendCooldownSeconds = 0,
            )
        }
    }

    private fun resendVerificationCode(emailOverride: String? = null) = intent {
        val email = (emailOverride ?: state.verificationEmail).trim()
        if (!isValidEmail(email)) {
            reduce { state.copy(verificationEmailError = AuthUiText.Resource(AuthValidationMessages.EMAIL_INVALID)) }
            return@intent
        }
        if (state.resendCooldownSeconds > 0) {
            return@intent
        }

        reduce {
            state.copy(
                verificationEmail = email,
                isSendingVerificationCode = true,
                verificationEmailError = null,
                verificationCodeError = null,
            )
        }

        sendEmailVerificationUseCase(email, OTPType.VERIFY_EMAIL)
            .onSuccess {
                reduce { state.copy(isSendingVerificationCode = false) }
                startCooldown(60)
                postSideEffect(
                    LoginSideEffect.ShowToast(
                        AuthUiText.Resource(
                            AuthValidationMessages.OTP_SENT_TO_EMAIL,
                            listOf(email),
                        ),
                    ),
                )
            }
            .onFailure { error ->
                val retryAfter = error.retryAfterSecondsOrNull()
                if (retryAfter != null) {
                    startCooldown(retryAfter)
                }

                val message = AuthErrorPresenter.resolveMessage(error, AuthValidationMessages.SEND_OTP_FAILED)
                reduce { state.copy(isSendingVerificationCode = false) }
                postSideEffect(LoginSideEffect.ShowToast(message))
            }
    }

    private fun verifyEmail() = intent {
        val email = state.verificationEmail.trim()
        val code = state.verificationCode.trim()

        if (!isValidEmail(email)) {
            reduce { state.copy(verificationEmailError = AuthUiText.Resource(AuthValidationMessages.EMAIL_INVALID)) }
            return@intent
        }
        if (code.isBlank()) {
            reduce { state.copy(verificationCodeError = AuthUiText.Resource(AuthValidationMessages.OTP_REQUIRED)) }
            return@intent
        }

        reduce {
            state.copy(
                isVerifyingEmail = true,
                verificationEmailError = null,
                verificationCodeError = null,
            )
        }

        verifyEmailUseCase(OTPType.VERIFY_EMAIL, email, code)
            .onSuccess {
                reduce {
                    state.copy(
                        isVerifyingEmail = false,
                        showEmailVerificationDialog = false,
                        verificationCode = "",
                        verificationCodeError = null,
                    )
                }
                postSideEffect(LoginSideEffect.ShowToast(AuthUiText.Resource(AuthValidationMessages.EMAIL_VERIFIED_SUCCESS)))
                autoLoginAfterVerification(email, state.password)
            }
            .onFailure { error ->
                val message = AuthErrorPresenter.resolveMessage(error, AuthValidationMessages.VERIFY_FAILED)
                reduce {
                    state.copy(
                        isVerifyingEmail = false,
                        verificationCodeError = message,
                    )
                }
            }
    }

    private fun autoLoginAfterVerification(email: String, password: String) = intent {
        if (password.isBlank()) return@intent

        reduce { state.copy(isLoading = true, formError = null, emailError = null, passwordError = null) }

        loginUserUseCase(email, password)
            .onSuccess {
                reduce { state.copy(isLoading = false) }
            }
            .onFailure { error ->
                val message = AuthErrorPresenter.resolveMessage(error, AuthValidationMessages.LOGIN_FAILED)
                reduce { state.copy(isLoading = false, formError = message) }
                postSideEffect(LoginSideEffect.ShowToast(message))
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
