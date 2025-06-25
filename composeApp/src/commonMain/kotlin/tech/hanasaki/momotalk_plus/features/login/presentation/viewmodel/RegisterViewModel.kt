package tech.hanasaki.momotalk_plus.features.login.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.features.login.domain.usecase.SignUpUserUseCase
import tech.hanasaki.momotalk_plus.features.login.presentation.state.RegisterIntent
import tech.hanasaki.momotalk_plus.features.login.presentation.state.RegisterSideEffect
import tech.hanasaki.momotalk_plus.features.login.presentation.state.RegisterState

class RegisterViewModel(
    private val registerUserUseCase: SignUpUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterState())
    val uiState: StateFlow<RegisterState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<RegisterSideEffect>()
    val sideEffect: Flow<RegisterSideEffect> = _sideEffect.receiveAsFlow()

    fun processIntent(intent: RegisterIntent) {
        viewModelScope.launch {
            // Clear previous error on new input
            _uiState.update { it.copy(error = null) }
            when (intent) {
                is RegisterIntent.EmailChanged ->
                    _uiState.update { it.copy(email = intent.email) }
                is RegisterIntent.PasswordChanged ->
                    _uiState.update { it.copy(password = intent.password) }
                is RegisterIntent.ConfirmPasswordChanged ->
                    _uiState.update { it.copy(confirmPassword = intent.confirmPassword) }
                is RegisterIntent.RegisterClicked ->
                    registerUser()
            }
        }
    }

    private suspend fun registerUser() {
        val currentState = _uiState.value
        if (currentState.password != currentState.confirmPassword) {
            _uiState.update { it.copy(error = "两次输入的密码不一致。") }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        when (val result = registerUserUseCase(currentState.email, currentState.password)) {
            is Result.Success -> {
                _uiState.update { it.copy(isLoading = false) }
                _sideEffect.send(RegisterSideEffect.NavigateToLogin)
            }
            is Result.Error -> {
                val errorMessage = result.error.message
                _uiState.update { it.copy(isLoading = false, error = errorMessage) }
                _sideEffect.send(RegisterSideEffect.ShowToast(errorMessage))
            }
        }
    }
}