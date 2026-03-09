package tech.hanasaki.momotalk_plus.features.chats.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
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
) : ViewModel(), ContainerHost<ChatsState, ChatsSideEffect> {

    override val container: Container<ChatsState, ChatsSideEffect> =
        viewModelScope.container(ChatsState())

    init {
        loadChats()
    }

    fun onIntent(intent: ChatsIntent) {
        when (intent) {
            is ChatsIntent.DeleteChat -> viewModelScope.launch { deleteChat(intent.chatId) }
            is ChatsIntent.PinChat -> intent { postSideEffect(ChatsSideEffect.ShowToast("暂不支持置顶会话")) }
            is ChatsIntent.ChatClicked -> intent { postSideEffect(ChatsSideEffect.NavigateToChatDetails(intent.chatId)) }
            is ChatsIntent.UpdateSearchQuery -> intent { reduce { state.copy(searchQuery = intent.query) } }
            is ChatsIntent.ShowCreateChatDialog -> {
                intent { reduce { state.copy(showCreateChatDialog = true) } }
                loadAvailableContacts()
            }

            is ChatsIntent.DismissCreateChatDialog -> intent { reduce { state.copy(showCreateChatDialog = false) } }
            is ChatsIntent.LoadAvailableContacts -> loadAvailableContacts()
            is ChatsIntent.CreateChat -> viewModelScope.launch {
                createChat(
                    characterId = intent.characterId,
                    title = intent.title,
                    description = intent.description,
                    avatarUrl = intent.avatarUrl,
                )
            }
        }
    }

    private fun loadChats() {
        getChatsUseCase()
            .onStart {
                intent { reduce { state.copy(isLoading = true) } }
            }
            .onEach { chats ->
                intent {
                    reduce {
                        state.copy(
                            isLoading = false,
                            chatList = chats
                        )
                    }
                }
            }
            .catch { e ->
                intent { reduce { state.copy(isLoading = false) } }
                intent { postSideEffect(ChatsSideEffect.ShowToast("加载会话失败: ${e.message}")) }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun deleteChat(chatId: String) {
        deleteChatUseCase(chatId)
            .onSuccess {
                intent { postSideEffect(ChatsSideEffect.ShowToast("删除会话成功")) }
            }.onFailure {
                intent { postSideEffect(ChatsSideEffect.ShowToast("删除会话失败: ${it.message}")) }
            }
    }

    private fun loadAvailableContacts() {
        contactProvider.getAvailableContacts()
            .onStart {
                intent { reduce { state.copy(isLoadingContacts = true) } }
            }
            .onEach { contact ->
                intent {
                    reduce {
                        state.copy(
                            isLoadingContacts = false,
                            availableContacts = contact
                        )
                    }
                }
            }
            .catch { e ->
                e.printStackTrace()
                intent { reduce { state.copy(isLoadingContacts = false) } }
                intent { postSideEffect(ChatsSideEffect.ShowToast("加载联系人失败: ${e.message}")) }
            }.launchIn(viewModelScope)
    }

    private suspend fun createChat(
        characterId: String,
        title: String,
        description: String,
        avatarUrl: String,
    ) {
        intent { reduce { state.copy(isCreatingChat = true) } }
        createChatUseCase(
            characterId,
            title,
            description,
            avatarUrl
        ).onSuccess {
            intent {
                reduce {
                    state.copy(
                        isCreatingChat = false,
                        showCreateChatDialog = false
                    )
                }
                postSideEffect(ChatsSideEffect.ShowToast("创建会话成功"))
            }
            loadChats()
        }.onFailure { e ->
            intent {
                reduce { state.copy(isCreatingChat = false) }
                postSideEffect(ChatsSideEffect.ShowToast("创建会话失败: ${e.message}"))
            }
        }
    }
}
