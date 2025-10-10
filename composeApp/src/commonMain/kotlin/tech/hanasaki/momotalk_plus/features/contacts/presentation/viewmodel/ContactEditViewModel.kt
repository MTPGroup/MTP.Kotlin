package tech.hanasaki.momotalk_plus.features.contacts.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.BaseViewModel
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
) :
    BaseViewModel<ContactEditState, ContactEditSideEffect, ContactEditIndent>(ContactEditState()) {
    private val characterId = savedStateHandle.toRoute<ContactsRoute.EditContact>().id

    init {
        loadContactInfo(characterId)
    }

    override fun processIntent(intent: ContactEditIndent) {
        viewModelScope.launch {
            when (intent) {
                is ContactEditIndent.NameChanged ->
                    updateState {
                        it.copy(name = intent.name)
                    }

                is ContactEditIndent.SignatureChanged ->
                    updateState {
                        it.copy(signature = intent.signature)
                    }

                is ContactEditIndent.PersonaChanged ->
                    updateState {
                        it.copy(persona = intent.persona)
                    }

                is ContactEditIndent.AvatarUrlChanged ->
                    updateState {
                        it.copy(avatarUrl = intent.avatarUrl)
                    }

                is ContactEditIndent.VisibilityChanged ->
                    updateState {
                        it.copy(visibility = intent.visibility)
                    }

                is ContactEditIndent.UpdateContactInfo ->
                    updateContactInfo(intent.id)

                is ContactEditIndent.UploadAvatar ->
                    uploadAvatar(intent.imageData, intent.userId)
            }
        }
    }

    private fun loadContactInfo(characterId: String) {
        characterDetailUseCase(characterId)
            .onStart {
                updateState {
                    it.copy(
                        isLoading = true,
                        errorMessage = null
                    )
                }
            }
            .onEach { character ->
                updateState {
                    it.copy(
                        isLoading = false,
                        name = character?.name ?: "",
                        signature = character?.signature ?: "",
                        persona = character?.persona ?: "",
                        avatarUrl = character?.avatarUrl ?: "",
                        visibility = character?.visibility ?: Visibility.PUBLIC,
                    )
                }
            }
            .catch { e ->
                e.printStackTrace()
                updateState {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
                sendSideEffect(
                    ContactEditSideEffect.ShowMessage("加载联系人信息失败: ${e.message}")
                )
            }
            .launchIn(viewModelScope)
    }

    private suspend fun updateContactInfo(id: String) {
        updateState {
            it.copy(
                isSaving = true,
                errorMessage = null
            )
        }
        val currentState = getState()
        updateContactUseCase(
            id = id,
            name = currentState.name,
            persona = currentState.persona,
            signature = currentState.signature,
            avatarUrl = currentState.avatarUrl,
            visibility = currentState.visibility,
        )
            .onSuccess {
                updateState {
                    it.copy(isSaving = false)
                }
                sendSideEffect(
                    ContactEditSideEffect.ShowMessage(
                        "联系人信息更新成功"
                    )
                )
            }
            .onFailure { e ->
                e.printStackTrace()
                sendSideEffect(
                    ContactEditSideEffect.ShowMessage(
                        "联系人信息更新失败: ${e.message}"
                    )
                )
            }
    }

    private suspend fun uploadAvatar(
        imageData: ImageData,
        userId: String?,
    ) {
        updateState {
            it.copy(isUploadingAvatar = true)
        }

        uploadImageUseCase(
            imageData,
            UploadPath.CHARACTER_AVATAR,
            userId
        ).onSuccess { avatar ->
            updateState {
                it.copy(
                    avatarUrl = avatar,
                    isUploadingAvatar = false
                )
            }
            sendSideEffect(
                ContactEditSideEffect.ShowMessage("头像上传成功")
            )
        }.onFailure { e ->
            updateState {
                it.copy(isUploadingAvatar = false)
            }
            sendSideEffect(
                ContactEditSideEffect.ShowMessage("头像上传失败: ${e.message}")
            )
        }
    }
}