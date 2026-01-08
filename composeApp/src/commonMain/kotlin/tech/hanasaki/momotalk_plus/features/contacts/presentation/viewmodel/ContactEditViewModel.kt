package tech.hanasaki.momotalk_plus.features.contacts.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import tech.hanasaki.momotalk_plus.core.domain.model.ImageData
import tech.hanasaki.momotalk_plus.core.domain.model.UploadPath
import tech.hanasaki.momotalk_plus.core.domain.model.Visibility
import tech.hanasaki.momotalk_plus.core.domain.usecase.CharacterDetailUseCase
import tech.hanasaki.momotalk_plus.core.domain.usecase.UpdateCharacterUseCase
import tech.hanasaki.momotalk_plus.core.domain.usecase.UploadImageUseCase
import tech.hanasaki.momotalk_plus.features.contacts.presentation.navigation.ContactsRoute
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactEditIndent
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactEditSideEffect
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactEditState

class ContactEditViewModel(
    private val updateContactUseCase: UpdateCharacterUseCase,
    private val characterDetailUseCase: CharacterDetailUseCase,
    private val uploadImageUseCase: UploadImageUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel(), ContainerHost<ContactEditState, ContactEditSideEffect> {

    override val container: Container<ContactEditState, ContactEditSideEffect> =
        viewModelScope.container(ContactEditState())

    private val characterId = savedStateHandle.toRoute<ContactsRoute.EditContact>().id

    init {
        loadContactInfo(characterId)
    }

    fun onIntent(intent: ContactEditIndent) {
        when (intent) {
            is ContactEditIndent.NameChanged -> intent { reduce { state.copy(name = intent.name) } }
            is ContactEditIndent.SignatureChanged -> intent { reduce { state.copy(signature = intent.signature) } }
            is ContactEditIndent.PersonaChanged -> intent { reduce { state.copy(persona = intent.persona) } }
            is ContactEditIndent.AvatarUrlChanged -> intent { reduce { state.copy(avatarUrl = intent.avatarUrl) } }
            is ContactEditIndent.VisibilityChanged -> intent { reduce { state.copy(visibility = intent.visibility) } }
            is ContactEditIndent.UpdateContactInfo -> viewModelScope.launch { updateContactInfo(intent.id) }
            is ContactEditIndent.UploadAvatar -> viewModelScope.launch { uploadAvatar(intent.imageData, intent.userId) }
        }
    }

    private fun loadContactInfo(characterId: String) {
        characterDetailUseCase(characterId)
            .onStart {
                intent {
                    reduce {
                        state.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    }
                }
            }
            .onEach { character ->
                intent {
                    reduce {
                        state.copy(
                            isLoading = false,
                            name = character?.name ?: "",
                            signature = character?.signature ?: "",
                            persona = character?.persona ?: "",
                            avatarUrl = character?.avatarUrl ?: "",
                            visibility = character?.visibility ?: Visibility.PUBLIC,
                        )
                    }
                }
            }
            .catch { e ->
                e.printStackTrace()
                intent {
                    reduce {
                        state.copy(
                            isLoading = false,
                            errorMessage = e.message
                        )
                    }
                    postSideEffect(
                        ContactEditSideEffect.ShowMessage("加载联系人信息失败: ${e.message}")
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun updateContactInfo(id: String) {
        intent {
            reduce {
                state.copy(
                    isSaving = true,
                    errorMessage = null
                )
            }
        }
        val currentState = container.stateFlow.value
        updateContactUseCase(
            id = id,
            name = currentState.name,
            persona = currentState.persona,
            signature = currentState.signature,
            avatarUrl = currentState.avatarUrl,
            visibility = currentState.visibility,
        )
            .onSuccess {
                intent {
                    reduce { state.copy(isSaving = false) }
                    postSideEffect(ContactEditSideEffect.ShowMessage("联系人信息更新成功"))
                }
            }
            .onFailure { e ->
                e.printStackTrace()
                intent {
                    reduce { state.copy(isSaving = false) }
                    postSideEffect(
                        ContactEditSideEffect.ShowMessage(
                            "联系人信息更新失败: ${e.message}"
                        )
                    )
                }
            }
    }

    private suspend fun uploadAvatar(
        imageData: ImageData,
        userId: String?,
    ) {
        intent { reduce { state.copy(isUploadingAvatar = true) } }

        uploadImageUseCase(
            imageData,
            UploadPath.AVATAR,
            userId
        ).onSuccess { avatar ->
            intent {
                reduce {
                    state.copy(
                        avatarUrl = avatar,
                        isUploadingAvatar = false
                    )
                }
                postSideEffect(ContactEditSideEffect.ShowMessage("头像上传成功"))
            }
        }.onFailure { e ->
            intent {
                reduce { state.copy(isUploadingAvatar = false) }
                postSideEffect(ContactEditSideEffect.ShowMessage("头像上传失败: ${e.message}"))
            }
        }
    }
}
