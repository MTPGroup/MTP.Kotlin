package tech.hanasaki.momotalk_plus.features.profile.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.BaseViewModel
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
) : BaseViewModel<ProfileState, ProfileSideEffect, ProfileIntent>(
    ProfileState()
) {
    init {
        loadProfile()
    }

    override fun processIntent(intent: ProfileIntent) {
        viewModelScope.launch {
            when (intent) {
                is ProfileIntent.StartEdit -> startEdit()
                is ProfileIntent.CancelEdit -> cancelEdit()
                is ProfileIntent.NameChanged -> updateName(intent.name)
                is ProfileIntent.SaveProfile -> saveProfile()
                is ProfileIntent.ChangePassword -> navigateToChangePassword()
                is ProfileIntent.Logout -> logout()
                is ProfileIntent.UploadAvatar -> uploadAvatar(intent.imageData, intent.userId)
            }
        }
    }

    private fun loadProfile() {
        updateState {
            it.copy(isLoading = true)
        }
        obverseCurrentUserUseCase()
            .onEach { user ->
                updateState {
                    it.copy(
                        user = user,
                        originAvatar = user?.avatar,
                        isLoading = false
                    )
                }
            }
            .catch { error ->
                error.printStackTrace()
                updateState {
                    it.copy(
                        isLoading = false,
                    )
                }
                sendSideEffect(ProfileSideEffect.ShowMessage("加载用户信息失败: ${error.message}"))
            }
            .launchIn(viewModelScope)
    }

    private fun startEdit() {
        updateState {
            it.copy(
                isEditing = true,
                editedName = it.user?.username ?: ""
            )
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun cancelEdit() {
        updateState {
            it.copy(
                user = it.user?.copy(avatar = it.originAvatar ?: ""),
                isEditing = false,
                editedName = it.user?.username ?: ""
            )
        }
    }

    private fun updateName(name: String) {
        updateState {
            it.copy(editedName = name)
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun saveProfile() {
        updateState { it.copy(isSaving = true) }
        val currentState = getState()
        updateUserProfileUseCase(
            id = currentState.user?.id ?: "",
            name = currentState.editedName,
            avatar = currentState.user?.avatar
        ).onSuccess {
            updateState {
                it.copy(
                    user = it.user?.copy(username = it.editedName),
                    originAvatar = it.user?.avatar,
                    isEditing = false,
                    isSaving = false,
                )
            }
            sendSideEffect(ProfileSideEffect.ShowMessage("个人资料已更新"))
        }.onFailure { e ->
            updateState { it.copy(isSaving = false) }
            sendSideEffect(ProfileSideEffect.ShowMessage("更新个人资料失败: ${e.message}"))
        }
    }

    private fun navigateToChangePassword() {
        viewModelScope.launch {
            sendSideEffect(ProfileSideEffect.NavigateToChangePassword)
        }
    }

    private suspend fun logout() {
        logoutUseCase()
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun uploadAvatar(
        imageData: ImageData,
        userId: String?,
    ) {
        updateState {
            it.copy(isUploadingAvatar = true)
        }

        uploadImageUseCase(
            imageData,
            UploadPath.AVATAR,
            userId
        ).onSuccess { response ->
            updateState {
                it.copy(
                    user = it.user?.copy(avatar = response),
                    isUploadingAvatar = false
                )
            }

        }.onFailure { e ->
            updateState {
                it.copy(isUploadingAvatar = false)
            }
            sendSideEffect(
                ProfileSideEffect.ShowMessage("头像上传失败: ${e.message}")
            )
        }
    }
}
