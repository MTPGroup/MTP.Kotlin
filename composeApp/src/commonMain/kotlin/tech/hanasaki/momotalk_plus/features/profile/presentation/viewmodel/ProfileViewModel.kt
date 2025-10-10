package tech.hanasaki.momotalk_plus.features.profile.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.BaseViewModel
import tech.hanasaki.momotalk_plus.core.domain.model.IResult
import tech.hanasaki.momotalk_plus.core.domain.model.ImageData
import tech.hanasaki.momotalk_plus.core.domain.model.UploadPath
import tech.hanasaki.momotalk_plus.core.domain.usecase.ObserveCurrentUserUseCase
import tech.hanasaki.momotalk_plus.core.domain.usecase.UploadImageUseCase
import tech.hanasaki.momotalk_plus.features.profile.domain.usecase.UpdateUserProfileUseCase
import tech.hanasaki.momotalk_plus.features.profile.presentation.state.ProfileIntent
import tech.hanasaki.momotalk_plus.features.profile.presentation.state.ProfileSideEffect
import tech.hanasaki.momotalk_plus.features.profile.presentation.state.ProfileState

class ProfileViewModel(
    private val obverseCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val uploadImageUseCase: UploadImageUseCase,
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
                editedName = it.user?.name ?: ""
            )
        }
    }

    private fun cancelEdit() {
        updateState {
            it.copy(
                user = it.user?.copy(avatar = it.originAvatar),
                isEditing = false,
                editedName = it.user?.name ?: ""
            )
        }
    }

    private fun updateName(name: String) {
        updateState {
            it.copy(editedName = name)
        }
    }

    private fun saveProfile() {
        viewModelScope.launch {
            updateState { it.copy(isSaving = true) }

            when (val result = updateUserProfileUseCase(
                name = uiState.value.editedName,
                image = uiState.value.user?.avatar
            )) {
                is IResult.Success -> {
                    updateState {
                        it.copy(
                            user = it.user?.copy(name = it.editedName),
                            originAvatar = it.user?.avatar,
                            isEditing = false,
                            isSaving = false,
                        )
                    }
                    sendSideEffect(ProfileSideEffect.ShowMessage("个人资料已更新"))
                }

                is IResult.Error -> {
                    updateState { it.copy(isSaving = false) }
                    sendSideEffect(ProfileSideEffect.ShowMessage(result.error.message))
                }
            }
        }
    }

    private fun navigateToChangePassword() {
        viewModelScope.launch {
            sendSideEffect(ProfileSideEffect.NavigateToChangePassword)
        }
    }

    private fun logout() {
        viewModelScope.launch {
            sendSideEffect(ProfileSideEffect.NavigateToLogin)
        }
    }

    private suspend fun uploadAvatar(
        imageData: ImageData,
        userId: String?,
    ) {
        try {
            updateState {
                it.copy(isUploadingAvatar = true)
            }

            when (val result = uploadImageUseCase(imageData, UploadPath.USER_AVATAR, userId)) {
                is IResult.Success -> {

                    updateState {
                        it.copy(
                            user = it.user?.copy(avatar = result.data),
                            isUploadingAvatar = false
                        )
                    }
                }

                is IResult.Error -> {
                    updateState {
                        it.copy(isUploadingAvatar = false)
                    }
                    sendSideEffect(
                        ProfileSideEffect.ShowMessage("头像上传失败: ${result.error.message}")
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            updateState {
                it.copy(isUploadingAvatar = false)
            }
            sendSideEffect(
                ProfileSideEffect.ShowMessage("头像上传失败: ${e.message}")
            )
        }
    }
}
