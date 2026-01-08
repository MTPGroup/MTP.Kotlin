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
) : ViewModel(), ContainerHost<ContactDetailState, ContactDetailSideEffect> {

    override val container: Container<ContactDetailState, ContactDetailSideEffect> =
        viewModelScope.container(ContactDetailState())

    private val characterId: String = savedStateHandle.toRoute<ContactsRoute.ContactDetail>().id

    init {
        loadContact(characterId)
    }

    fun onIntent(intent: ContactDetailIntent) {
        when (intent) {
            is ContactDetailIntent.ShowDeleteDialog -> intent {
                reduce {
                    state.copy(
                        showDialog = true,
                        errorMessage = null,
                    )
                }
            }

            is ContactDetailIntent.DeleteContact -> viewModelScope.launch { deleteContact(intent.userId) }
        }
    }

    private fun loadContact(characterId: String) {
        characterDetailUseCase(characterId)
            .onStart {
                intent { reduce { state.copy(isLoading = true) } }
            }
            .onEach { contact ->
                contact?.let {
                    intent {
                        reduce {
                            state.copy(
                                contact = contact,
                                isLoading = false
                            )
                        }
                    }
                }
            }
            .catch { e ->
                e.printStackTrace()
                intent { reduce { state.copy(isLoading = false) } }
                intent { postSideEffect(ContactDetailSideEffect.ShowErrorMessage(e.message ?: "未知错误")) }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun deleteContact(characterId: String) {
        deleteContactUseCase(characterId)
            .onSuccess {
                intent {
                    reduce { state.copy(showDialog = false) }
                    postSideEffect(ContactDetailSideEffect.NavigateToContactsList)
                }
            }
            .onFailure { e ->
                intent { postSideEffect(ContactDetailSideEffect.ShowErrorMessage("删除联系人失败: ${e.message}")) }
            }
    }
}
