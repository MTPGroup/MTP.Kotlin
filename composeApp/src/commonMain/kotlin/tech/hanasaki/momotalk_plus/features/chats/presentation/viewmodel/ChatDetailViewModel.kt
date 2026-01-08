package tech.hanasaki.momotalk_plus.features.chats.presentation.viewmodel

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
import tech.hanasaki.momotalk_plus.core.domain.model.User
import tech.hanasaki.momotalk_plus.features.chats.domain.model.*
import tech.hanasaki.momotalk_plus.features.chats.domain.usecase.ClearChatHistoryUseCase
import tech.hanasaki.momotalk_plus.features.chats.domain.usecase.GetChatHistoryUseCase
import tech.hanasaki.momotalk_plus.features.chats.domain.usecase.GetChatInfoUseCase
import tech.hanasaki.momotalk_plus.features.chats.domain.usecase.SendMessageStreamUseCase
import tech.hanasaki.momotalk_plus.features.chats.presentation.navigation.ChatsRoute
import tech.hanasaki.momotalk_plus.features.chats.presentation.state.ChatDetailIntent
import tech.hanasaki.momotalk_plus.features.chats.presentation.state.ChatDetailSideEffect
import tech.hanasaki.momotalk_plus.features.chats.presentation.state.ChatDetailState
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ChatDetailViewModel(
    private val getChatInfoUseCase: GetChatInfoUseCase,
    private val getChatHistoryUseCase: GetChatHistoryUseCase,
    private val sendMessageStreamUseCase: SendMessageStreamUseCase,
    private val clearChatHistoryUseCase: ClearChatHistoryUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), ContainerHost<ChatDetailState, ChatDetailSideEffect> {

    override val container: Container<ChatDetailState, ChatDetailSideEffect> =
        viewModelScope.container(ChatDetailState())

    private val chatId = savedStateHandle.toRoute<ChatsRoute.ChatDetail>().chatId

    init {
        loadChat(chatId)
    }

    fun onIntent(intent: ChatDetailIntent) {
        when (intent) {
            is ChatDetailIntent.InputMessageChanged -> intent { reduce { state.copy(inputMessage = intent.message) } }
            is ChatDetailIntent.SendMessage -> viewModelScope.launch {
                sendMessage(
                    intent.chatId,
                    intent.message,
                    intent.currentUser,
                )
            }

            is ChatDetailIntent.ClearChatHistory -> viewModelScope.launch { clearChatHistory(intent.chatId) }
            is ChatDetailIntent.ClearError -> intent { reduce { state.copy(error = null) } }
        }
    }

    private fun loadChat(chatId: String) {
        getChatInfoUseCase(chatId)
            .onStart {
                intent { reduce { state.copy(isLoading = true, chatId = chatId) } }
            }.onEach { info ->
                intent {
                    reduce {
                        state.copy(
                            isLoading = false,
                            title = info.title,
                            avatar = info.avatarUrl,
                            characterName = info.character.name,
                            characterAvatar = info.character.avatarUrl,
                            error = null
                        )
                    }
                }
            }.catch { e ->
                intent { reduce { state.copy(isLoading = false) } }
                intent { postSideEffect(ChatDetailSideEffect.ShowToast("加载聊天信息失败: ${e.message}")) }
            }.launchIn(viewModelScope)

        getChatHistoryUseCase(chatId)
            .onEach { messages ->
                intent {
                    reduce { state.copy(messages = messages, error = null) }
                    postSideEffect(ChatDetailSideEffect.ScrollToBottom)
                }
            }.catch { e ->
                intent { reduce { state.copy(isLoading = false) } }
                intent { postSideEffect(ChatDetailSideEffect.ShowToast("加载聊天记录失败: ${e.message}")) }
            }.launchIn(viewModelScope)
    }

    @OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
    private suspend fun sendMessage(
        chatId: String,
        message: String,
        currentUser: User?,
    ) {
        val userMessage = Message(
            id = Uuid.random().toString(),
            chatId = chatId,
            content = message,
            role = MessageSenderRole.USER,
            sender = MessageSender(
                name = currentUser?.username ?: "",
                avatar = currentUser?.avatar
            ),
            type = MessageType.TEXT,
            createdAt = Clock.System.now().toString(),
            updatedAt = Clock.System.now().toString()
        )
        intent {
            reduce {
                state.copy(
                    messages = state.messages + userMessage,
                )
            }
        }

        val currentState = container.stateFlow.value
        val streamingMessageId = Uuid.random().toString()
        val streamingMessage = Message(
            id = streamingMessageId,
            chatId = chatId,
            content = "",
            role = MessageSenderRole.AI,
            sender = MessageSender(
                name = currentState.characterName,
                avatar = currentState.characterAvatar,
            ),
            type = MessageType.TEXT,
            isStreaming = true,
            createdAt = Clock.System.now().toString(),
            updatedAt = Clock.System.now().toString()
        )
        intent {
            reduce {
                state.copy(
                    messages = state.messages + streamingMessage,
                    isStreaming = true,
                    isTyping = true,
                    streamingMessageId = streamingMessageId,
                )
            }
        }

        var finalAiContent = ""
        sendMessageStreamUseCase(chatId, message).collect { event ->
            when (event) {
                is StreamEvent.Token -> {
                    finalAiContent += event.content
                    intent {
                        reduce {
                            val updatedMessages = state.messages.map { msg ->
                                if (msg.id == streamingMessageId) {
                                    msg.copy(content = finalAiContent)
                                } else {
                                    msg
                                }
                            }
                            state.copy(messages = updatedMessages)
                        }
                    }
                }

                is StreamEvent.ReflectionChunk -> {
                }

                is StreamEvent.Final -> {
                    intent {
                        reduce {
                            val finalMessages = state.messages.map { msg ->
                                if (msg.id == streamingMessageId) {
                                    msg.copy(
                                        content = finalAiContent,
                                        isStreaming = false,
                                    )
                                } else {
                                    msg
                                }
                            }
                            state.copy(
                                messages = finalMessages,
                                isStreaming = false,
                                isTyping = false,
                                streamingMessageId = null,
                            )
                        }
                        postSideEffect(ChatDetailSideEffect.ScrollToBottom)
                    }
                }

                is StreamEvent.Error -> {
                    intent {
                        reduce {
                            state.copy(
                                messages = state.messages.filter { msg ->
                                    msg.id != streamingMessageId
                                },
                                isStreaming = false,
                                isTyping = false,
                                streamingMessageId = null,
                                error = event.content
                            )
                        }
                        postSideEffect(ChatDetailSideEffect.ShowToast(event.content))
                    }
                }
            }
        }
    }

    private suspend fun clearChatHistory(chatId: String) {
        clearChatHistoryUseCase(chatId)
            .onSuccess {
                intent { reduce { state.copy(messages = emptyList()) } }
            }
            .onFailure { e ->
                intent { postSideEffect(ChatDetailSideEffect.ShowToast("清除聊天记录失败: ${e.message}")) }
            }
    }
}
