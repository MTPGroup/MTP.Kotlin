package tech.hanasaki.momotalk_plus.features.settings.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.BaseViewModel
import tech.hanasaki.momotalk_plus.core.theme.AppTheme
import tech.hanasaki.momotalk_plus.core.theme.ThemeManager
import tech.hanasaki.momotalk_plus.features.settings.domain.usecase.*
import tech.hanasaki.momotalk_plus.features.settings.presentation.state.SettingsIntent
import tech.hanasaki.momotalk_plus.features.settings.presentation.state.SettingsSideEffect
import tech.hanasaki.momotalk_plus.features.settings.presentation.state.SettingsState

class SettingsViewModel(
    private val themeManager: ThemeManager,
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val saveThemeUseCase: SaveThemeUseCase,
    private val saveNotificationSettingsUseCase: SaveNotificationSettingsUseCase,
    private val saveSoundSettingsUseCase: SaveSoundSettingsUseCase,
    private val saveVibrationSettingsUseCase: SaveVibrationSettingsUseCase,
) : BaseViewModel<SettingsState, SettingsSideEffect, SettingsIntent>(
    initialState = SettingsState()
) {
    init {
        loadSettings()
    }

    override fun processIntent(intent: SettingsIntent) {
        viewModelScope.launch {
            when (intent) {
                is SettingsIntent.ThemeChanged -> changeTheme(intent.theme)
                is SettingsIntent.NotificationsToggled -> toggleNotifications(intent.enabled)
                is SettingsIntent.SoundToggled -> toggleSound(intent.enabled)
                is SettingsIntent.VibrationToggled -> toggleVibration(intent.enabled)
                is SettingsIntent.AboutClicked -> navigateToAbout()
                is SettingsIntent.PrivacyPolicyClicked -> navigateToPrivacyPolicy()
                is SettingsIntent.TermsOfServiceClicked -> navigateToTermsOfService()
            }
        }
    }

    private fun loadSettings() {
        getUserSettingsUseCase()
            .onEach { userSettings ->
                val availableThemes = themeManager.getAvailableThemes()
                val savedTheme = availableThemes.find { it.id == userSettings.theme.name.lowercase() }
                    ?: availableThemes.first()

                // 应用保存的主题
                themeManager.setTheme(savedTheme)

                updateState {
                    it.copy(
                        currentTheme = savedTheme,
                        availableThemes = availableThemes,
                        notificationsEnabled = userSettings.notificationsEnabled,
                        soundEnabled = userSettings.soundEnabled,
                        vibrationEnabled = userSettings.vibrationEnabled
                    )
                }

            }.catch { e ->
                sendSideEffect(SettingsSideEffect.ShowMessage("加载设置失败: ${e.message}"))
            }.launchIn(viewModelScope)
    }

    private suspend fun changeTheme(theme: AppTheme) {
        // 保存主题到持久化存储
        saveThemeUseCase(theme.id)

        // 应用主题
        themeManager.setTheme(theme)
        updateState { it.copy(currentTheme = theme) }

        sendSideEffect(SettingsSideEffect.ShowMessage("主题已切换"))
    }

    private suspend fun toggleNotifications(enabled: Boolean) {
        saveNotificationSettingsUseCase(enabled)
        updateState { it.copy(notificationsEnabled = enabled) }
    }

    private suspend fun toggleSound(enabled: Boolean) {
        saveSoundSettingsUseCase(enabled)
        updateState { it.copy(soundEnabled = enabled) }
    }

    private suspend fun toggleVibration(enabled: Boolean) {
        saveVibrationSettingsUseCase(enabled)
        updateState { it.copy(vibrationEnabled = enabled) }
    }

    private suspend fun navigateToAbout() {
        sendSideEffect(SettingsSideEffect.NavigateToAbout)
    }

    private suspend fun navigateToPrivacyPolicy() {
        sendSideEffect(SettingsSideEffect.NavigateToPrivacyPolicy)
    }

    private suspend fun navigateToTermsOfService() {
        sendSideEffect(SettingsSideEffect.NavigateToTermsOfService)
    }
}
