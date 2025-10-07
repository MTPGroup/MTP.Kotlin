package tech.hanasaki.momotalk_plus.features.chats.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.BaseViewModel
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.core.domain.repository.ContactProvider
import tech.hanasaki.momotalk_plus.features.chats.domain.usecase.CreateChatUseCase
import tech.hanasaki.momotalk_plus.features.chats.domain.usecase.DeleteChatUseCase
import tech.hanasaki.momotalk_plus.features.chats.domain.usecase.GetChatsUseCase
import tech.hanasaki.momotalk_plus.features.chats.presentation.state.ChatsIntent
import tech.hanasaki.momotalk_plus.features.chats.presentation.state.ChatsSideEffect
import tech.hanasaki.momotalk_plus.features.chats.presentation.state.ChatsState

class ChatsViewModel(
    private val getChatsUseCase: GetChatsUseCase,
    private val contactProvider: ContactProvider,
    private val createChatUseCase: CreateChatUseCase,
    private val deleteChatUseCase: DeleteChatUseCase,
) : BaseViewModel<ChatsState, ChatsSideEffect, ChatsIntent>(ChatsState()) {
    override fun processIntent(intent: ChatsIntent) {
        viewModelScope.launch {
            when (intent) {
                is ChatsIntent.LoadChats ->
                    loadChats()

                is ChatsIntent.DeleteChat ->
                    deleteChat(intent.chatId)

                is ChatsIntent.PinChat ->
                    sendSideEffect(ChatsSideEffect.ShowToast("暂不支持置顶会话"))

                is ChatsIntent.ChatClicked ->
                    sendSideEffect(ChatsSideEffect.NavigateToChatDetails(intent.chatId))

                is ChatsIntent.UpdateSearchQuery ->
                    updateState {
                        it.copy(
                            searchQuery = intent.query
                        )
                    }

                is ChatsIntent.ShowCreateChatDialog -> {
                    updateState {
                        it.copy(
                            showCreateChatDialog = true
                        )
                    }
                    loadAvailableContacts()
                }

                is ChatsIntent.DismissCreateChatDialog ->
                    updateState {
                        it.copy(
                            showCreateChatDialog = false
                        )
                    }

                is ChatsIntent.LoadAvailableContacts ->
                    loadAvailableContacts()

                is ChatsIntent.CreateChat ->
                    createChat(
                        characterId = intent.characterId,
                        title = intent.title,
                        description = intent.description,
                        avatarUrl = intent.avatarUrl,
                    )
            }
        }
    }

    private suspend fun loadChats() {
        updateState { it.copy(isLoading = true) }
        when (val result = getChatsUseCase()) {
            is IResult.Success -> {
                updateState {
                    it.copy(
                        isLoading = false,
                        chatList = result.data
                    )
                }
            }

            is IResult.Error -> {
                updateState {
                    it.copy(
                        isLoading = false,
                        error = result.error.message
                    )
                }
                sendSideEffect(ChatsSideEffect.ShowToast(result.error.message))
            }
        }
    }

    private suspend fun deleteChat(chatId: String) {
        when (val result = deleteChatUseCase(chatId)) {
            is IResult.Success -> {
                sendSideEffect(ChatsSideEffect.ShowToast("删除会话成功"))
                loadChats()
            }

            is IResult.Error -> {
                updateState {
                    it.copy(
                        error = result.error.message
                    )
                }
                sendSideEffect(ChatsSideEffect.ShowToast(result.error.message))
            }
        }
    }

    private suspend fun loadAvailableContacts() {
        updateState { it.copy(isLoadingContacts = true) }
        when (val result = contactProvider.getAvailableContacts()) {
            is IResult.Success -> {
                updateState {
                    it.copy(
                        isLoadingContacts = false,
                        availableContacts = result.data
                    )
                }
            }

            is IResult.Error -> {
                updateState {
                    it.copy(
                        isLoadingContacts = false,
                        error = result.error.message
                    )
                }
                sendSideEffect(ChatsSideEffect.ShowToast(result.error.message))
            }
        }
    }

    private suspend fun createChat(
        characterId: String,
        title: String,
        description: String,
        avatarUrl: String,
    ) {
        updateState { it.copy(isCreatingChat = true) }
        when (val result = createChatUseCase(characterId, title, description, avatarUrl)) {
            is IResult.Success -> {
                updateState {
                    it.copy(
                        isCreatingChat = false,
                        showCreateChatDialog = false
                    )
                }
                sendSideEffect(ChatsSideEffect.ShowToast("创建会话成功"))
                loadChats()
            }

            is IResult.Error -> {
                println("create chat error: ${result.error.message}")
                updateState {
                    it.copy(
                        isCreatingChat = false,
                        error = result.error.message
                    )
                }
                sendSideEffect(ChatsSideEffect.ShowToast(result.error.message))
            }
        }
    }
}