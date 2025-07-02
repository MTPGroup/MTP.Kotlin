package tech.hanasaki.momotalk_plus.features.login.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.features.login.domain.usecase.*
import tech.hanasaki.momotalk_plus.features.login.presentation.state.RegisterIntent
import tech.hanasaki.momotalk_plus.features.login.presentation.state.RegisterSideEffect
import tech.hanasaki.momotalk_plus.features.login.presentation.state.RegisterState

private fun isValidEmail(email: String): Boolean {
    val emailRegex = Regex(
        "[a-zA-Z0-9+._%\\-]{1,256}" +
                "@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
    )
    return emailRegex.matches(email)
}

class RegisterViewModel(
    private val registerUserUseCase: SignUpUserUseCase,
    private val sendVerificationCodeUseCase: SendVerificationCodeUseCase,
    private val verifyCodeUseCase: VerifyCodeUseCase,
    private val getImageCaptchaUseCase: GetImageCaptchaUseCase,
    private val verifyCaptchaUseCase: VerifyCaptchaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterState())
    val uiState: StateFlow<RegisterState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<RegisterSideEffect>()
    val sideEffect: Flow<RegisterSideEffect> = _sideEffect.receiveAsFlow()

    fun processIntent(intent: RegisterIntent) {
        viewModelScope.launch {
            if (intent !is RegisterIntent.RequestVerificationCode && intent !is RegisterIntent.SubmitCaptcha) {
                _uiState.update { it.copy(error = null) }
            }
            when (intent) {
                is RegisterIntent.BindingChanged ->
                    _uiState.update { it.copy(binding = intent.email) }

                is RegisterIntent.UsernameChanged ->
                    _uiState.update { it.copy(username = intent.username) }

                is RegisterIntent.PasswordChanged ->
                    _uiState.update { it.copy(password = intent.password) }

                is RegisterIntent.ConfirmPasswordChanged ->
                    _uiState.update { it.copy(confirmPassword = intent.confirmPassword) }

                is RegisterIntent.VerificationCodeChanged ->
                    _uiState.update { it.copy(verificationCode = intent.token) }

                is RegisterIntent.CaptchaInputChanged ->
                    _uiState.update { it.copy(captchaInput = intent.input) }

                is RegisterIntent.VerifyCodeAndProceed ->
                    verifyCodeAndProceed()

                is RegisterIntent.RegisterClicked ->
                    registerUser()

                is RegisterIntent.RequestVerificationCode ->
                    fetchCaptcha()

                is RegisterIntent.SubmitCaptcha ->
                    verifyCaptchaAndRequestCode()

                is RegisterIntent.DismissCaptchaDialog ->
                    _uiState.update { it.copy(showCaptchaDialog = false) }

                RegisterIntent.RequestCaptcha ->
                    fetchCaptcha()
            }
        }
    }


    private suspend fun verifyCodeAndProceed() {
        val currentState = _uiState.value
        if (currentState.verificationCode.isBlank()) {
            _uiState.update { it.copy(error = "请输入验证码。") }
            return
        }
        _uiState.update { it.copy(isLoading = true) }

        when (val tokenResult = verifyCodeUseCase(
            currentState.verificationId,
            currentState.verificationCode
        )) {
            is Result.Success -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        verificationToken = tokenResult.data,
                        error = null
                    )
                }
                _sideEffect.send(RegisterSideEffect.NavigateToNextStep)
            }

            is Result.Error -> {
                _uiState.update { it.copy(isLoading = false, error = tokenResult.error.message) }
                _sideEffect.send(RegisterSideEffect.ShowToast(tokenResult.error.message))
            }
        }
    }

    private suspend fun registerUser() {
        val currentState = _uiState.value
        if (currentState.password != currentState.confirmPassword) {
            _uiState.update { it.copy(error = "两次输入的密码不一致。") }
            return
        }
        if (currentState.verificationToken.isBlank()) {
            _uiState.update { it.copy(error = "请先完成上一步的账户验证。") }
            return
        }

        val isEmail = isValidEmail(currentState.binding)
        val email = if (isEmail) currentState.binding else null
        val phoneNumber = if (!isEmail) currentState.binding else null

        when (val result = registerUserUseCase(
            email = email,
            phoneNumber = phoneNumber,
            username = currentState.username,
            password = currentState.password,
            verificationToken = currentState.verificationToken
        )) {
            is Result.Success -> {
                _sideEffect.send(RegisterSideEffect.NavigateToSuccessStep)
            }

            is Result.Error -> {
                val errorMessage = result.error.message
                _uiState.update { it.copy(error = errorMessage) }
                _sideEffect.send(RegisterSideEffect.ShowToast(errorMessage))
            }
        }
    }

    private suspend fun fetchCaptcha() {
        _uiState.update { it.copy(error = null) }
        when (val result = getImageCaptchaUseCase()) {
            is Result.Success -> {
                _uiState.update {
                    it.copy(
                        captchaImage = result.data.data,
                        captchaToken = result.data.token,
                        showCaptchaDialog = true,
                        captchaInput = "" // 清空旧的输入
                    )
                }
            }

            is Result.Error -> {
                _uiState.update { it.copy(error = result.error.message) }
                _sideEffect.send(RegisterSideEffect.ShowToast(result.error.message))
            }
        }
    }

    private suspend fun verifyCaptchaAndRequestCode() {
        _uiState.update { it.copy(isRequestingCode = true, error = null) }
        val currentState = _uiState.value

        // 验证图片验证码
        when (val verifyResult =
            verifyCaptchaUseCase(currentState.captchaToken, currentState.captchaInput)) {
            is Result.Success -> {
                // 验证成功，返回 `verificationId`
                val captchaToken = verifyResult.data
                println("Captcha token: $captchaToken")

                // 使用 `verificationId` 发送邮件/短信验证码
                sendFinalVerificationCode(captchaToken)
            }

            is Result.Error -> {
                // 验证失败，显示错误并刷新图片验证码
                val errorMessage = verifyResult.error.message
                _uiState.update { it.copy(isRequestingCode = false, error = errorMessage) }
                _sideEffect.send(RegisterSideEffect.ShowToast(errorMessage))
                fetchCaptcha() // 刷新图片验证码
            }
        }
    }

    private suspend fun sendFinalVerificationCode(captchaToken: String) {
        val currentState = _uiState.value
        val isEmail = isValidEmail(currentState.binding)
        val email = if (isEmail) currentState.binding else null
        val phoneNumber = if (!isEmail) currentState.binding else null

        println("Sending verification code to: $email or $phoneNumber with captcha token: $captchaToken")

        when (val sendResult = sendVerificationCodeUseCase(email, phoneNumber, captchaToken)) {
            is Result.Success -> {
                _uiState.update {
                    it.copy(
                        isRequestingCode = false,
                        showCaptchaDialog = false,
                        verificationId = sendResult.data,
                        error = null
                    )
                }
                _sideEffect.send(RegisterSideEffect.ShowToast("验证码已发送，请查收。"))
            }

            is Result.Error -> {
                // 如果第二步失败，同样显示错误并刷新图片验证码
                val errorMessage = sendResult.error.message
                println("Error sending verification code: $errorMessage")
                _uiState.update { it.copy(isRequestingCode = false, error = errorMessage) }
                _sideEffect.send(RegisterSideEffect.ShowToast(errorMessage))
                fetchCaptcha()
            }
        }
    }
}