package tech.hanasaki.momotalk_plus.features.profile.presentation.state

import tech.hanasaki.momotalk_plus.core.data.model.UserProfile

/**
 * ProfileState - 个人资料页面状态
 */
data class ProfileState(
    val user: UserProfile? = null,
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val editedName: String = "",
    val isSaving: Boolean = false,
)

/**
 * ProfileIntent - 个人资料页面意图
 */
sealed interface ProfileIntent {
    data object LoadProfile : ProfileIntent
    data object StartEdit : ProfileIntent
    data object CancelEdit : ProfileIntent
    data class NameChanged(val name: String) : ProfileIntent
    data object SaveProfile : ProfileIntent
    data object ChangePassword : ProfileIntent
    data object Logout : ProfileIntent
}

/**
 * ProfileSideEffect - 个人资料页面副作用
 */
sealed interface ProfileSideEffect {
    data class ShowMessage(val message: String) : ProfileSideEffect
    data object NavigateToChangePassword : ProfileSideEffect
    data object NavigateToLogin : ProfileSideEffect
}
