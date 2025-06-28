package tech.hanasaki.momotalk_plus.features.chats.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.features.chats.presentation.state.ChatListItem
import tech.hanasaki.momotalk_plus.features.chats.presentation.state.ChatsIntent
import tech.hanasaki.momotalk_plus.features.chats.presentation.state.ChatsSideEffect
import tech.hanasaki.momotalk_plus.features.chats.presentation.state.ChatsState

class ChatsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ChatsState())
    val uiState: StateFlow<ChatsState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<ChatsSideEffect>()
    val sideEffect: Flow<ChatsSideEffect> = _sideEffect.receiveAsFlow()

    init {
        loadChats()
    }

    fun processIntent(intent: ChatsIntent) {
        viewModelScope.launch {
            when (intent) {
                is ChatsIntent.ChatClicked -> {
                    _sideEffect.send(ChatsSideEffect.NavigateToChatDetails(intent.chatId))
                }
            }
        }
    }

    private fun loadChats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(1500) // 模拟网络延迟
            val mockChats = listOf(
                ChatListItem("1", null, "爱丽丝", "明天见！", "10:48", 2),
                ChatListItem("2", null, "柚子", "好的，部长！", "昨天", 0),
                ChatListItem("3", null, "优香", "预算申请已驳回。", "星期一", 1),
            )
            _uiState.update { it.copy(isLoading = false, chatList = mockChats) }
        }
    }
}