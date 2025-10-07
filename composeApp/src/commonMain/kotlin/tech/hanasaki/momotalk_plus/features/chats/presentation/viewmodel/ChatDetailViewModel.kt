package tech.hanasaki.momotalk_plus.features.chats.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.BaseViewModel
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.core.data.model.UserProfile
import tech.hanasaki.momotalk_plus.features.chats.domain.model.*
import tech.hanasaki.momotalk_plus.features.chats.domain.usecase.ClearChatHistoryUseCase
import tech.hanasaki.momotalk_plus.features.chats.domain.usecase.GetChatHistoryUseCase
import tech.hanasaki.momotalk_plus.features.chats.domain.usecase.GetChatInfoUseCase
import tech.hanasaki.momotalk_plus.features.chats.domain.usecase.SendMessageStreamUseCase
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
) : BaseViewModel<ChatDetailState, ChatDetailSideEffect, ChatDetailIntent>(ChatDetailState()) {

    override fun processIntent(intent: ChatDetailIntent) {
        viewModelScope.launch {
            when (intent) {
                is ChatDetailIntent.LoadChat -> loadChat(intent.chatId)
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
        viewModelScope.launch {
            updateState { it.copy(isLoading = true, chatId = chatId) }

            when (val info = getChatInfoUseCase(chatId)) {
                is IResult.Success -> {
                    when (val history = getChatHistoryUseCase(chatId)) {
                        is IResult.Success -> {
                            updateState {
                                it.copy(
                                    isLoading = false,
                                    title = info.data.title,
                                    avatar = info.data.avatarUrl,
                                    characterName = info.data.character.name,
                                    characterAvatar = info.data.character.avatarUrl,
                                    messages = history.data,
                                    error = null
                                )
                            }
                        }

                        is IResult.Error -> {
                            updateState {
                                it.copy(
                                    isLoading = false,
                                    error = history.error.message
                                )
                            }
                            sendSideEffect(ChatDetailSideEffect.ShowToast(history.error.message))
                        }
                    }
                }

                is IResult.Error -> {
                    updateState {
                        it.copy(
                            isLoading = false,
                            error = info.error.message
                        )
                    }
                    sendSideEffect(ChatDetailSideEffect.ShowToast(info.error.message))
                    return@launch
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
    private fun sendMessage(
        chatId: String,
        message: String,
        currentUser: UserProfile?,
    ) {
        viewModelScope.launch {
            val userMessage = Message(
                id = Uuid.random().toString(),
                chatId = chatId,
                content = message,
                role = MessageSenderRole.USER,
                sender = MessageSender(
                    name = currentUser?.name ?: "",
                    avatar = currentUser?.image
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
            sendMessageStreamUseCase(chatId, message).collect { result ->
                when (result) {
                    is IResult.Success -> {
                        when (val event = result.data) {
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

                    is IResult.Error -> {
                        updateState {
                            it.copy(
                                messages = it.messages.filter { msg ->
                                    msg.id != streamingMessageId
                                },
                                isStreaming = false,
                                isTyping = false,
                                streamingMessageId = null,
                                error = result.error.message
                            )
                        }
                        sendSideEffect(ChatDetailSideEffect.ShowToast(result.error.message))
                    }
                }
            }
        }
    }

    private suspend fun clearChatHistory(chatId: String) {
        when (val result = clearChatHistoryUseCase(chatId)) {
            is IResult.Success -> {
                updateState { it.copy(messages = emptyList()) }
            }

            is IResult.Error -> {
                updateState { it.copy(error = result.error.message) }
                sendSideEffect(ChatDetailSideEffect.ShowToast(result.error.message))
            }
        }
    }
}