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
import tech.hanasaki.momotalk_plus.core.domain.usecase.LogoutUserUseCase

class AppViewModel(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getLoginStateUseCase: GetLoginStateUseCase,
    private val logoutUserUseCase: LogoutUserUseCase
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

    /**
     * 登出用户（临时测试方法）
     */
    fun logout() {
        viewModelScope.launch {
            // 清除当前用户状态
            _uiState.update { it.copy(currentUser = null, isLoggedIn = false) }
            // 这里可以添加更多的登出逻辑，比如清除缓存等
            logoutUserUseCase()
        }
    }
}