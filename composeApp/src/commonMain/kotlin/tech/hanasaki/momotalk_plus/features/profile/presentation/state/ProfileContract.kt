package tech.hanasaki.momotalk_plus.features.profile.presentation.state

import tech.hanasaki.momotalk_plus.core.domain.model.ImageData
import tech.hanasaki.momotalk_plus.core.domain.model.User

/**
 * ProfileState - 个人资料页面状态
 */
data class ProfileState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val originAvatar: String? = null,
    val editedName: String = "",
    val isSaving: Boolean = false,
    val isUploadingAvatar: Boolean = false,
)

/**
 * ProfileIntent - 个人资料页面意图
 */
sealed interface ProfileIntent {
    data object StartEdit : ProfileIntent
    data object CancelEdit : ProfileIntent
    data class NameChanged(val name: String) : ProfileIntent
    data object SaveProfile : ProfileIntent
    data object ChangePassword : ProfileIntent
    data object Logout : ProfileIntent
    data class UploadAvatar(val imageData: ImageData) : ProfileIntent
}

/**
 * ProfileSideEffect - 个人资料页面副作用
 */
sealed interface ProfileSideEffect {
    data class ShowMessage(val message: String) : ProfileSideEffect
    data object NavigateToChangePassword : ProfileSideEffect
    data object NavigateToLogin : ProfileSideEffect
}
