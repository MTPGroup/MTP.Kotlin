package tech.hanasaki.momotalk_plus.features.chats.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.BaseViewModel
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
) : BaseViewModel<ChatDetailState, ChatDetailSideEffect, ChatDetailIntent>(ChatDetailState()) {
    private val chatId = savedStateHandle.toRoute<ChatsRoute.ChatDetail>().chatId

    init {
        loadChat(chatId)
    }

    override fun processIntent(intent: ChatDetailIntent) {
        viewModelScope.launch {
            when (intent) {
                is ChatDetailIntent.InputMessageChanged ->
                    updateState { it.copy(inputMessage = intent.message) }

                is ChatDetailIntent.SendMessage ->
                    sendMessage(
                        intent.chatId,
                        intent.message,
                        intent.currentUser,
                    )

                is ChatDetailIntent.ClearChatHistory ->
                    clearChatHistory(intent.chatId)

                is ChatDetailIntent.ClearError -> updateState { it.copy(error = null) }
            }
        }
    }

    private fun loadChat(chatId: String) {
        getChatInfoUseCase(chatId)
            .onStart {
                updateState { it.copy(isLoading = true, chatId = chatId) }
            }.onEach { info ->
                updateState {
                    it.copy(
                        isLoading = false,
                        title = info.title,
                        avatar = info.avatarUrl,
                        characterName = info.character.name,
                        characterAvatar = info.character.avatarUrl,
                        error = null
                    )
                }
            }.catch { e ->
                updateState {
                    it.copy(
                        isLoading = false,
                    )
                }
                sendSideEffect(ChatDetailSideEffect.ShowToast("加载聊天信息失败: ${e.message}"))
            }.launchIn(viewModelScope)

        getChatHistoryUseCase(chatId)
            .onEach { messages ->
                updateState {
                    it.copy(
                        messages = messages,
                        error = null
                    )
                }
                sendSideEffect(ChatDetailSideEffect.ScrollToBottom)
            }.catch { e ->
                updateState {
                    it.copy(
                        isLoading = false,
                    )
                }
                sendSideEffect(ChatDetailSideEffect.ShowToast("加载聊天记录失败: ${e.message}"))
            }.launchIn(viewModelScope)
    }

    @OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
    private fun sendMessage(
        chatId: String,
        message: String,
        currentUser: User?,
    ) {
        viewModelScope.launch {
            val userMessage = Message(
                id = Uuid.random().toString(),
                chatId = chatId,
                content = message,
                role = MessageSenderRole.USER,
                sender = MessageSender(
                    name = currentUser?.name ?: "",
                    avatar = currentUser?.avatar
                ),
                type = MessageType.TEXT,
                createdAt = Clock.System.now().toString(),
                updatedAt = Clock.System.now().toString()
            )
            updateState {
                it.copy(
                    messages = it.messages + userMessage,
                )
            }

            val currentState = getState()
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
            updateState {
                it.copy(
                    messages = it.messages + streamingMessage,
                    isStreaming = true,
                    isTyping = true,
                    streamingMessageId = streamingMessageId,
                )
            }

            var finalAiContent = ""
            sendMessageStreamUseCase(chatId, message).collect { event ->
                when (event) {
                    is StreamEvent.Token -> {
                        finalAiContent += event.content
                        updateState {
                            val updatedMessages = it.messages.map { msg ->
                                if (msg.id == streamingMessageId) {
                                    msg.copy(content = finalAiContent)
                                } else {
                                    msg
                                }
                            }
                            it.copy(
                                messages = updatedMessages
                            )
                        }
                    }

                    is StreamEvent.ReflectionChunk -> {
                    }

                    is StreamEvent.Final -> {
                        updateState {
                            val finalMessages = it.messages.map { msg ->
                                if (msg.id == streamingMessageId) {
                                    msg.copy(
                                        content = finalAiContent,
                                        isStreaming = false,
                                    )
                                } else {
                                    msg
                                }
                            }
                            it.copy(
                                messages = finalMessages,
                                isStreaming = false,
                                isTyping = false,
                                streamingMessageId = null,
                            )
                        }
                        sendSideEffect(ChatDetailSideEffect.ScrollToBottom)
                    }

                    is StreamEvent.Error -> {
                        updateState {
                            it.copy(
                                messages = it.messages.filter { msg ->
                                    msg.id != streamingMessageId
                                },
                                isStreaming = false,
                                isTyping = false,
                                streamingMessageId = null,
                                error = event.content
                            )
                        }
                        sendSideEffect(ChatDetailSideEffect.ShowToast(event.content))
                    }
                }
            }
        }
    }

    private suspend fun clearChatHistory(chatId: String) {

        clearChatHistoryUseCase(chatId)
            .onSuccess {
                updateState { it.copy(messages = emptyList()) }
            }
            .onFailure { e ->
                sendSideEffect(ChatDetailSideEffect.ShowToast("清除聊天记录失败: ${e.message}"))
            }
    }
}