package tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.core.utils.isValidEmail
import tech.hanasaki.momotalk_plus.features.auth.domain.usecase.*
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.ForgotPasswordIntent
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.ForgotPasswordSideEffect
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.ForgotPasswordState

class ForgotPasswordViewModel(
    private val getImageCaptchaUseCase: GetImageCaptchaUseCase,
    private val verifyCaptchaUseCase: VerifyCaptchaUseCase,
    private val sendVerificationCodeUseCase: SendVerificationCodeUseCase,
    private val verifyCodeUseCase: VerifyCodeUseCase,
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
                    _uiState.update { it.copy(verificationCode = intent.code) }

                is ForgotPasswordIntent.SendVerificationCode ->
                    sendVerificationCode(intent.captchaToken)

                is ForgotPasswordIntent.CaptchaInputChanged ->
                    _uiState.update { it.copy(captchaInput = intent.input) }

                ForgotPasswordIntent.VerifyCaptcha ->
                    verifyCaptcha()

                ForgotPasswordIntent.GetCaptcha ->
                    fetchCaptcha()

                ForgotPasswordIntent.VerifyCode ->
                    verifyCode()

                ForgotPasswordIntent.DismissCaptchaDialog ->
                    _uiState.update { it.copy(showCaptchaDialog = false) }
            }
        }
    }

    private suspend fun fetchCaptcha() {
        _uiState.update { it.copy(error = null) }
        when (val result = getImageCaptchaUseCase()) {
            is Result.Success -> {
                _uiState.update {
                    it.copy(
                        showCaptchaDialog = true,
                        captchaImage = result.data.data,
                        captchaToken = result.data.token,
                        captchaInput = "",
                    )
                }
            }

            is Result.Error -> {
                val errorMessage = result.error.message
                _uiState.update { it.copy(error = errorMessage) }
                _sideEffect.send(ForgotPasswordSideEffect.ShowToast(errorMessage))
            }
        }
    }

    private suspend fun verifyCaptcha() {
        _uiState.update { it.copy(error = null) }
        val currentState = _uiState.value

        when (val result = verifyCaptchaUseCase(currentState.captchaToken, currentState.captchaInput)) {
            is Result.Success -> {
                _uiState.update { it.copy(showCaptchaDialog = false) }
                sendVerificationCode(result.data)
            }

            is Result.Error -> {
                val errorMessage = result.error.message
                _uiState.update { it.copy(error = errorMessage) }
                _sideEffect.send(ForgotPasswordSideEffect.ShowToast(errorMessage))
                fetchCaptcha() // 刷新图片验证码
            }
        }
    }

    private suspend fun verifyCode() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        val currentState = _uiState.value

        when (val result = verifyCodeUseCase(currentState.verificationId, currentState.verificationCode)) {
            is Result.Success -> {
                _uiState.update { it.copy(isLoading = false) }
                val isEmail = isValidEmail(currentState.email)
                val email = if (isEmail) currentState.email else null
                val phoneNumber = if (isEmail) null else currentState.email

                when (val resetResult = resetPasswordUseCase(
                    email = email,
                    phoneNumber = phoneNumber,
                    newPassword = currentState.newPassword,
                    verificationToken = result.data
                )) {
                    is Result.Success -> {
                        _uiState.update { it.copy(newPassword = "", verificationCode = "") }
                        _sideEffect.send(ForgotPasswordSideEffect.NavigateToSuccess)
                    }

                    is Result.Error -> {
                        val errorMessage = resetResult.error.message
                        _uiState.update { it.copy(isLoading = false, error = errorMessage) }
                        _sideEffect.send(ForgotPasswordSideEffect.ShowToast(errorMessage))
                    }
                }
            }

            is Result.Error -> {
                val errorMessage = result.error.message
                _uiState.update { it.copy(isLoading = false, error = errorMessage) }
                _sideEffect.send(ForgotPasswordSideEffect.ShowToast(errorMessage))
            }
        }
    }

    private suspend fun sendVerificationCode(captchaToken: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        when (val result =
            sendVerificationCodeUseCase(uiState.value.email, phoneNumber = null, captchaToken)) {
            is Result.Success -> {
                _uiState.update { it.copy(isLoading = false, verificationId = result.data) }
                _sideEffect.send(ForgotPasswordSideEffect.ShowToast("重置链接已发送，请检查您的邮箱。"))
            }

            is Result.Error -> {
                val errorMessage = result.error.message
                _uiState.update { it.copy(isLoading = false, error = errorMessage) }
                _sideEffect.send(ForgotPasswordSideEffect.ShowToast(errorMessage))
            }
        }
    }
}