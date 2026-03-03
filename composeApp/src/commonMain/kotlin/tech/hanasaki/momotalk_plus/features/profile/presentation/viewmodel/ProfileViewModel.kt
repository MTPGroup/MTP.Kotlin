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
import tech.hanasaki.momotalk_plus.core.domain.usecase.LogoutUseCase
import tech.hanasaki.momotalk_plus.core.domain.usecase.ObserveCurrentUserUseCase
import tech.hanasaki.momotalk_plus.core.network.AppErrorException
import tech.hanasaki.momotalk_plus.core.network.toDisplayMessage
import tech.hanasaki.momotalk_plus.features.profile.domain.usecase.UpdateUserProfileUseCase
import tech.hanasaki.momotalk_plus.features.profile.domain.usecase.UploadAvatarUseCase
import tech.hanasaki.momotalk_plus.features.profile.presentation.state.ProfileIntent
import tech.hanasaki.momotalk_plus.features.profile.presentation.state.ProfileSideEffect
import tech.hanasaki.momotalk_plus.features.profile.presentation.state.ProfileState
import kotlin.time.ExperimentalTime

class ProfileViewModel(
    private val obverseCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val uploadAvatarUseCase: UploadAvatarUseCase,
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
            is ProfileIntent.UploadAvatar -> viewModelScope.launch { uploadAvatar(intent.imageData) }
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
            .catch { e ->
                val msg = when (e) {
                    is AppErrorException -> e.appError.toDisplayMessage()
                    else -> "加载用户信息失败，请稍后重试"
                }
                intent {
                    reduce { state.copy(isLoading = false) }
                    postSideEffect(ProfileSideEffect.ShowMessage(msg))
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
        runCatching {
            updateUserProfileUseCase(
                username = currentState.editedName,
                avatar = currentState.user?.avatar
            )
        }.onSuccess {
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
            val msg = when (e) {
                is AppErrorException -> e.appError.toDisplayMessage()
                is IllegalArgumentException -> e.message ?: "输入不合法"
                else -> "更新个人资料失败，请稍后重试"
            }
            intent {
                reduce { state.copy(isSaving = false) }
                postSideEffect(ProfileSideEffect.ShowMessage(msg))
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
    ) {
        intent { reduce { state.copy(isUploadingAvatar = true) } }

        runCatching {
            uploadAvatarUseCase(
                imageData,
            )
        }.onSuccess { response ->
            intent {
                reduce {
                    state.copy(
                        user = state.user?.copy(avatar = response),
                        isUploadingAvatar = false
                    )
                }
            }

        }.onFailure { e ->
            val msg = when (e) {
                is AppErrorException -> e.appError.toDisplayMessage()
                else -> "头像上传失败，请稍后重试"
            }
            intent {
                reduce { state.copy(isUploadingAvatar = false) }
                postSideEffect(
                    ProfileSideEffect.ShowMessage(msg)
                )
            }
        }
    }
}
