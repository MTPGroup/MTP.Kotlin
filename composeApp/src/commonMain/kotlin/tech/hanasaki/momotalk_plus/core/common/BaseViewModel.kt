package tech.hanasaki.momotalk_plus.core.common

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

abstract class BaseViewModel<T, U, V>(
    initialState: T,
) : ViewModel() {
    private val _uiState = MutableStateFlow(initialState)
    val uiState = _uiState.asStateFlow()

    private val _sideEffect = Channel<U>()
    val sideEffect = _sideEffect.receiveAsFlow()

    abstract fun processIntent(intent: V)

    protected fun getState(): T = _uiState.value

    protected fun updateState(update: (T) -> T) {
        _uiState.value = update(_uiState.value)
    }

    protected suspend fun sendSideEffect(effect: U) {
        _sideEffect.send(effect)
    }
}