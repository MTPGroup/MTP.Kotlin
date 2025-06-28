package tech.hanasaki.momotalk_plus.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.app.state.AppUiState
import tech.hanasaki.momotalk_plus.core.domain.usecase.GetLoginStateUseCase
import tech.hanasaki.momotalk_plus.core.domain.usecase.GetUserInfoUseCase
import tech.hanasaki.momotalk_plus.core.domain.usecase.RefreshIdTokenUseCase

class AppViewModel(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getLoginStateUseCase: GetLoginStateUseCase,
    private val refreshIdTokenUseCase: RefreshIdTokenUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        checkUserLoginStatus()
    }

    private fun checkUserLoginStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getLoginStateUseCase().collect { idToken ->
                if (idToken == null) {
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = false) }
                } else {
                    // 如果用户已登录，先刷新ID令牌，再使用ID令牌获取用户信息
                    refreshIdTokenUseCase()
                    getUserInfoUseCase(idToken).fold(
                        onSuccess = { user ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    isLoggedIn = true,
                                    currentUser = user,
                                )
                            }
                        },
                        onError = {
                            _uiState.update { it.copy(isLoading = false, isLoggedIn = false) }
                        }
                    )
                }
            }
        }
    }

    /**
     * 当用户登录成功后，使用UID获取并更新全局用户状态
     */
    fun onLoginSuccess(uid: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getUserInfoUseCase(uid).fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(isLoading = false, currentUser = user)
                    }
                },
                onError = {
                    _uiState.update { it.copy(isLoading = false) }
                }
            )
        }
    }
}