package tech.hanasaki.momotalk_plus.features.profile.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.BaseViewModel
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.core.domain.model.ImageData
import tech.hanasaki.momotalk_plus.core.domain.model.UploadPath
import tech.hanasaki.momotalk_plus.core.domain.usecase.GetUserInfoUseCase
import tech.hanasaki.momotalk_plus.core.domain.usecase.UploadImageUseCase
import tech.hanasaki.momotalk_plus.features.profile.domain.usecase.UpdateUserProfileUseCase
import tech.hanasaki.momotalk_plus.features.profile.presentation.state.ProfileIntent
import tech.hanasaki.momotalk_plus.features.profile.presentation.state.ProfileSideEffect
import tech.hanasaki.momotalk_plus.features.profile.presentation.state.ProfileState

class ProfileViewModel(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val uploadImageUseCase: UploadImageUseCase,
) : BaseViewModel<ProfileState, ProfileSideEffect, ProfileIntent>(
    ProfileState()
) {

    override fun processIntent(intent: ProfileIntent) {
        viewModelScope.launch {
            when (intent) {
                is ProfileIntent.LoadProfile -> loadProfile()
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

    private suspend fun loadProfile() {
        updateState {
            it.copy(isLoading = false)
        }
        when (val result = getUserInfoUseCase()) {
            is IResult.Success -> {
                updateState {
                    it.copy(
                        user = result.data,
                        originAvatar = result.data?.image,
                        isLoading = false
                    )
                }
            }

            is IResult.Error -> {
                updateState {
                    it.copy(isLoading = false)
                }
                sendSideEffect(ProfileSideEffect.ShowMessage(result.error.message))
            }
        }
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
                user = it.user?.copy(image = it.originAvatar),
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
                image = uiState.value.user?.image
            )) {
                is IResult.Success -> {
                    updateState {
                        it.copy(
                            user = it.user?.copy(name = it.editedName),
                            originAvatar = it.user?.image,
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
                            user = it.user?.copy(image = result.data),
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
