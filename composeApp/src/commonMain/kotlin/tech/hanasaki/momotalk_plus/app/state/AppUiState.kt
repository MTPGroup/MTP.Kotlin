package tech.hanasaki.momotalk_plus.app.state

import tech.hanasaki.momotalk_plus.core.domain.model.User

data class AppUiState(
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
)