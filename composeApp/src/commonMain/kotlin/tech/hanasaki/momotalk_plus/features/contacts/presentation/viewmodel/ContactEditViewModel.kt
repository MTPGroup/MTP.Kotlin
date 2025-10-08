package tech.hanasaki.momotalk_plus.features.contacts.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.BaseViewModel
import tech.hanasaki.momotalk_plus.core.domain.model.IResult
import tech.hanasaki.momotalk_plus.core.domain.model.ImageData
import tech.hanasaki.momotalk_plus.core.domain.model.UploadPath
import tech.hanasaki.momotalk_plus.core.domain.usecase.CharacterDetailUseCase
import tech.hanasaki.momotalk_plus.core.domain.usecase.UpdateCharacterUseCase
import tech.hanasaki.momotalk_plus.core.domain.usecase.UploadImageUseCase
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactEditIndent
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactEditSideEffect
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactEditState

class ContactEditViewModel(
    private val updateContactUseCase: UpdateCharacterUseCase,
    private val characterDetailUseCase: CharacterDetailUseCase,
    private val uploadImageUseCase: UploadImageUseCase,
) :
    BaseViewModel<ContactEditState, ContactEditSideEffect, ContactEditIndent>(ContactEditState()) {

    override fun processIntent(intent: ContactEditIndent) {
        viewModelScope.launch {
            when (intent) {
                is ContactEditIndent.LoadContactInfo ->
                    loadContactInfo(intent.contactId)

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

    private suspend fun loadContactInfo(characterId: String) {
        updateState {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }
        when (val result = characterDetailUseCase(characterId)) {
            is IResult.Success -> {
                updateState {
                    it.copy(
                        isLoading = false,
                        name = result.data.name,
                        signature = result.data.signature,
                        persona = result.data.persona,
                        avatarUrl = result.data.avatarUrl,
                        visibility = result.data.visibility,
                    )
                }
            }

            is IResult.Error -> {
                sendSideEffect(
                    ContactEditSideEffect.ShowMessage(result.error.message)
                )
            }
        }
    }

    private suspend fun updateContactInfo(id: String) {
        updateState {
            it.copy(
                isSaving = true,
                errorMessage = null
            )
        }
        val currentState = getState()
        when (val result = updateContactUseCase(
            id = id,
            name = currentState.name,
            signature = currentState.signature,
            persona = currentState.persona,
            avatarUrl = currentState.avatarUrl,
            visibility = currentState.visibility,
        )) {
            is IResult.Success -> {
                updateState {
                    it.copy(isSaving = false)
                }
                sendSideEffect(
                    ContactEditSideEffect.ShowMessage(
                        "联系人信息更新成功"
                    )
                )
            }

            is IResult.Error -> {
                updateState {
                    it.copy(
                        isSaving = false,
                        errorMessage = result.error.message
                    )
                }
                sendSideEffect(
                    ContactEditSideEffect.ShowMessage(
                        "联系人信息更新失败: ${result.error.message}"
                    )
                )
            }
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

            when (val result = uploadImageUseCase(imageData, UploadPath.CHARACTER_AVATAR, userId)) {
                is IResult.Success -> {
                    updateState {
                        it.copy(
                            avatarUrl = result.data,
                            isUploadingAvatar = false
                        )
                    }
                    sendSideEffect(
                        ContactEditSideEffect.ShowMessage("头像上传成功")
                    )
                }

                is IResult.Error -> {
                    updateState {
                        it.copy(isUploadingAvatar = false)
                    }
                    sendSideEffect(
                        ContactEditSideEffect.ShowMessage("头像上传失败: ${result.error.message}")
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            updateState {
                it.copy(isUploadingAvatar = false)
            }
            sendSideEffect(
                ContactEditSideEffect.ShowMessage("头像上传失败: ${e.message}")
            )
        }
    }
}