package tech.hanasaki.momotalk_plus.features.profile.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.BaseViewModel
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.features.profile.domain.usecase.UpdateUserProfileUseCase
import tech.hanasaki.momotalk_plus.features.profile.presentation.state.ProfileIntent
import tech.hanasaki.momotalk_plus.features.profile.presentation.state.ProfileSideEffect
import tech.hanasaki.momotalk_plus.features.profile.presentation.state.ProfileState

class ProfileViewModel(
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
) : BaseViewModel<ProfileState, ProfileSideEffect, ProfileIntent>(
) {

    override fun processIntent(intent: ProfileIntent) {
            when (intent) {
                is ProfileIntent.LoadProfile -> loadProfile()
                is ProfileIntent.StartEdit -> startEdit()
                is ProfileIntent.CancelEdit -> cancelEdit()
                is ProfileIntent.NameChanged -> updateName(intent.name)
                is ProfileIntent.SaveProfile -> saveProfile()
                is ProfileIntent.ChangePassword -> navigateToChangePassword()
                is ProfileIntent.Logout -> logout()
            }
        }

        updateState {
            it.copy(isLoading = false)
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
}
