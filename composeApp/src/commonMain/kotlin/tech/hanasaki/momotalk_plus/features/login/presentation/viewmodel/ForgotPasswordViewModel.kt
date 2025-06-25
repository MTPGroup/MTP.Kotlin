package tech.hanasaki.momotalk_plus.features.login.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.features.login.domain.usecase.SendResetPasswordEmailUseCase
import tech.hanasaki.momotalk_plus.features.login.presentation.state.ForgotPasswordIntent
import tech.hanasaki.momotalk_plus.features.login.presentation.state.ForgotPasswordSideEffect
import tech.hanasaki.momotalk_plus.features.login.presentation.state.ForgotPasswordState

class ForgotPasswordViewModel(
    private val sendResetPasswordEmailUseCase: SendResetPasswordEmailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordState())
    val uiState: StateFlow<ForgotPasswordState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<ForgotPasswordSideEffect>()
    val sideEffect: Flow<ForgotPasswordSideEffect> = _sideEffect.receiveAsFlow()

    fun processIntent(intent: ForgotPasswordIntent) {
        viewModelScope.launch {
            when (intent) {
                is ForgotPasswordIntent.EmailChanged -> {
                    _uiState.update { it.copy(email = intent.email, error = null) }
                }
                is ForgotPasswordIntent.SendResetLink -> {
                    sendResetEmail()
                }
            }
        }
    }

    private suspend fun sendResetEmail() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        when (val result = sendResetPasswordEmailUseCase(uiState.value.email)) {
            is Result.Success -> {
                _uiState.update { it.copy(isLoading = false, emailSent = true) }
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