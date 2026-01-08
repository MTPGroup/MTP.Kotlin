package tech.hanasaki.momotalk_plus.features.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import tech.hanasaki.momotalk_plus.core.domain.model.ImageData
import tech.hanasaki.momotalk_plus.core.domain.model.UploadPath
import tech.hanasaki.momotalk_plus.core.domain.usecase.LogoutUseCase
import tech.hanasaki.momotalk_plus.core.domain.usecase.ObserveCurrentUserUseCase
import tech.hanasaki.momotalk_plus.core.domain.usecase.UploadImageUseCase
import tech.hanasaki.momotalk_plus.features.profile.domain.usecase.UpdateUserProfileUseCase
import tech.hanasaki.momotalk_plus.features.profile.presentation.state.ProfileIntent
import tech.hanasaki.momotalk_plus.features.profile.presentation.state.ProfileSideEffect
import tech.hanasaki.momotalk_plus.features.profile.presentation.state.ProfileState
import kotlin.time.ExperimentalTime

class ProfileViewModel(
    private val obverseCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val uploadImageUseCase: UploadImageUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel(), ContainerHost<ProfileState, ProfileSideEffect> {

    override val container: Container<ProfileState, ProfileSideEffect> =
        viewModelScope.container(ProfileState())

    init {
        loadProfile()
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.StartEdit -> startEdit()
            is ProfileIntent.CancelEdit -> cancelEdit()
            is ProfileIntent.NameChanged -> updateName(intent.name)
            is ProfileIntent.SaveProfile -> viewModelScope.launch { saveProfile() }
            is ProfileIntent.ChangePassword -> navigateToChangePassword()
            is ProfileIntent.Logout -> viewModelScope.launch { logout() }
            is ProfileIntent.UploadAvatar -> viewModelScope.launch { uploadAvatar(intent.imageData, intent.userId) }
        }
    }

    private fun loadProfile() {
        intent { reduce { state.copy(isLoading = true) } }
        obverseCurrentUserUseCase()
            .onEach { user ->
                intent {
                    reduce {
                        state.copy(
                            user = user,
                            originAvatar = user?.avatar,
                            isLoading = false
                        )
                    }
                }
            }
            .catch { error ->
                error.printStackTrace()
                intent {
                    reduce { state.copy(isLoading = false) }
                    postSideEffect(ProfileSideEffect.ShowMessage("加载用户信息失败: ${error.message}"))
                }
            }
            .launchIn(viewModelScope)
    }

    private fun startEdit() {
        intent {
            reduce {
                state.copy(
                    isEditing = true,
                    editedName = state.user?.username ?: ""
                )
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun cancelEdit() {
        intent {
            reduce {
                state.copy(
                    user = state.user?.copy(avatar = state.originAvatar ?: ""),
                    isEditing = false,
                    editedName = state.user?.username ?: ""
                )
            }
        }
    }

    private fun updateName(name: String) {
        intent { reduce { state.copy(editedName = name) } }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun saveProfile() {
        intent { reduce { state.copy(isSaving = true) } }
        val currentState = container.stateFlow.value
        updateUserProfileUseCase(
            id = currentState.user?.id ?: "",
            name = currentState.editedName,
            avatar = currentState.user?.avatar
        ).onSuccess {
            intent {
                reduce {
                    state.copy(
                        user = state.user?.copy(username = state.editedName),
                        originAvatar = state.user?.avatar,
                        isEditing = false,
                        isSaving = false,
                    )
                }
                postSideEffect(ProfileSideEffect.ShowMessage("个人资料已更新"))
            }
        }.onFailure { e ->
            intent {
                reduce { state.copy(isSaving = false) }
                postSideEffect(ProfileSideEffect.ShowMessage("更新个人资料失败: ${e.message}"))
            }
        }
    }

    private fun navigateToChangePassword() {
        intent { postSideEffect(ProfileSideEffect.NavigateToChangePassword) }
    }

    private suspend fun logout() {
        logoutUseCase()
        intent { postSideEffect(ProfileSideEffect.NavigateToLogin) }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun uploadAvatar(
        imageData: ImageData,
        userId: String?,
    ) {
        intent { reduce { state.copy(isUploadingAvatar = true) } }

        uploadImageUseCase(
            imageData,
            UploadPath.AVATAR,
            userId
        ).onSuccess { response ->
            intent {
                reduce {
                    state.copy(
                        user = state.user?.copy(avatar = response),
                        isUploadingAvatar = false
                    )
                }
            }

        }.onFailure { e ->
            intent {
                reduce { state.copy(isUploadingAvatar = false) }
                postSideEffect(
                    ProfileSideEffect.ShowMessage("头像上传失败: ${e.message}")
                )
            }
        }
    }
}
