package tech.hanasaki.momotalk_plus.features.contacts.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.core.domain.usecase.CharacterDetailUseCase
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactDetailIntent
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactDetailSideEffect
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactDetailState

class ContactDetailViewModel(
    private val characterDetailUseCase: CharacterDetailUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ContactDetailState())
    val uiState: StateFlow<ContactDetailState> = _uiState

    private val _sideEffect = Channel<ContactDetailSideEffect>()
    val sideEffect: Flow<ContactDetailSideEffect> = _sideEffect.receiveAsFlow()

    fun processIntent(intent: ContactDetailIntent) {
        viewModelScope.launch {
            when (intent) {
                is ContactDetailIntent.LoadContact ->
                    loadContact(intent.userId)

                is ContactDetailIntent.RefreshContact -> {
                    TODO()
                }

                is ContactDetailIntent.ChangeContact -> {
                    TODO()
                }
            }
        }
    }

    private suspend fun loadContact(characterId: String) {
        when (val result = characterDetailUseCase(characterId)) {
            is IResult.Success -> {
                _uiState.update {
                    it.copy(
                        contact = result.data
                    )
                }
            }

            is IResult.Error -> {
                _sideEffect.send(ContactDetailSideEffect.ShowErrorMessage(result.error.message))
            }
        }
    }
}