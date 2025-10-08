package tech.hanasaki.momotalk_plus.app.state

import tech.hanasaki.momotalk_plus.core.data.model.UserProfile

/**
 * 代表整个应用的全局UI状态
 * @param currentUser 当前登录的用户，如果未登录则为null
 * @param isLoading 是否正在加载用户信息
 * @param isInitialized 应用是否已完成初始化检查
 */
data class AppUiState(
    val currentUser: UserProfile? = null,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isInitialized: Boolean = false,
)