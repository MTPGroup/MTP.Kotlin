package tech.hanasaki.momotalk_plus.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.app.state.AppUiState
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.core.domain.usecase.GetLoginStateUseCase
import tech.hanasaki.momotalk_plus.core.domain.usecase.GetUserInfoUseCase
import tech.hanasaki.momotalk_plus.core.domain.usecase.LogoutUserUseCase

class AppViewModel(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getLoginStateUseCase: GetLoginStateUseCase,
    private val logoutUserUseCase: LogoutUserUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        checkUserLoginStatus()
    }

    private fun checkUserLoginStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val userInfo = getUserInfoUseCase()) {
                is IResult.Success -> {
                    println("User info fetched: ${userInfo.data}")
                    if (userInfo.data != null) {
                        _uiState.update {
                            it.copy(
                                isLoggedIn = true,
                                currentUser = userInfo.data,
                                isLoading = false
                            )
                        }
                    }
                }

                is IResult.Error -> {
                    val isLoggedIn = getLoginStateUseCase()
                    println("Error fetching user info: ${userInfo.error}. Login state: $isLoggedIn")
                    _uiState.update {
                        it.copy(
                            isLoggedIn = isLoggedIn,
                            currentUser = null,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    /**
     * 登出用户
     */
    fun logout() {
        viewModelScope.launch {
            logoutUserUseCase()
        }
    }
}