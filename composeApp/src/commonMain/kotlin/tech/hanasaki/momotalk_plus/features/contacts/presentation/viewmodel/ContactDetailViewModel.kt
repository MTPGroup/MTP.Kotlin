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
import tech.hanasaki.momotalk_plus.core.domain.usecase.CharacterDetailUseCase
import tech.hanasaki.momotalk_plus.features.contacts.domain.usecase.DeleteContactUseCase
import tech.hanasaki.momotalk_plus.features.contacts.presentation.navigation.ContactsRoute
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactDetailIntent
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactDetailSideEffect
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactDetailState

class ContactDetailViewModel(
    private val characterDetailUseCase: CharacterDetailUseCase,
    private val deleteContactUseCase: DeleteContactUseCase,
    private val savedStateHandle: SavedStateHandle,
) : BaseViewModel<ContactDetailState, ContactDetailSideEffect, ContactDetailIntent>(ContactDetailState()) {
    private val characterId: String = savedStateHandle.toRoute<ContactsRoute.ContactDetail>().id

    init {
        loadContact(characterId)
    }

    override fun processIntent(intent: ContactDetailIntent) {
        viewModelScope.launch {
            when (intent) {
                is ContactDetailIntent.ShowDeleteDialog ->
                    updateState {
                        it.copy(
                            showDialog = true,
                            errorMessage = null,
                        )
                    }

                is ContactDetailIntent.DeleteContact ->
                    deleteContact(intent.userId)
            }
        }
    }

    private fun loadContact(characterId: String) {
        characterDetailUseCase(characterId)
            .onStart {
                updateState { it.copy(isLoading = true) }
            }
            .onEach { contact ->
                contact?.let {
                    updateState {
                        it.copy(
                            contact = contact,
                            isLoading = false
                        )
                    }
                }
            }
            .catch { e ->
                e.printStackTrace()
                updateState { it.copy(isLoading = false) }
                sendSideEffect(ContactDetailSideEffect.ShowErrorMessage(e.message ?: "未知错误"))
            }
            .launchIn(viewModelScope)
    }

    private suspend fun deleteContact(characterId: String) {
        deleteContactUseCase(characterId)
            .onSuccess {
                updateState {
                    it.copy(
                        showDialog = false
                    )
                }
                sendSideEffect(ContactDetailSideEffect.NavigateToContactsList)
            }
            .onFailure { e ->
                sendSideEffect(ContactDetailSideEffect.ShowErrorMessage("删除联系人失败: ${e.message}"))
            }
    }
}