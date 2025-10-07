package tech.hanasaki.momotalk_plus.features.settings.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.BaseViewModel
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
        processIntent(SettingsIntent.LoadSettings)
    }

    override fun processIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.LoadSettings -> loadSettings()
            is SettingsIntent.ThemeChanged -> changeTheme(intent.theme)
            is SettingsIntent.NotificationsToggled -> toggleNotifications(intent.enabled)
            is SettingsIntent.SoundToggled -> toggleSound(intent.enabled)
            is SettingsIntent.VibrationToggled -> toggleVibration(intent.enabled)
            is SettingsIntent.AboutClicked -> navigateToAbout()
            is SettingsIntent.PrivacyPolicyClicked -> navigateToPrivacyPolicy()
            is SettingsIntent.TermsOfServiceClicked -> navigateToTermsOfService()
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            // 加载用户设置
            val userSettings = getUserSettingsUseCase()
            val availableThemes = themeManager.getAvailableThemes()
            val savedTheme = availableThemes.find { it.id == userSettings.themeId }
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
        }
    }

    private fun changeTheme(theme: tech.hanasaki.momotalk_plus.core.theme.AppTheme) {
        viewModelScope.launch {
            // 保存主题到持久化存储
            saveThemeUseCase(theme.id)

            // 应用主题
            themeManager.setTheme(theme)
            updateState { it.copy(currentTheme = theme) }

            sendSideEffect(SettingsSideEffect.ShowMessage("主题已切换"))
        }
    }

    private fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            saveNotificationSettingsUseCase(enabled)
            updateState { it.copy(notificationsEnabled = enabled) }
        }
    }

    private fun toggleSound(enabled: Boolean) {
        viewModelScope.launch {
            saveSoundSettingsUseCase(enabled)
            updateState { it.copy(soundEnabled = enabled) }
        }
    }

    private fun toggleVibration(enabled: Boolean) {
        viewModelScope.launch {
            saveVibrationSettingsUseCase(enabled)
            updateState { it.copy(vibrationEnabled = enabled) }
        }
    }

    private fun navigateToAbout() {
        viewModelScope.launch {
            sendSideEffect(SettingsSideEffect.NavigateToAbout)
        }
    }

    private fun navigateToPrivacyPolicy() {
        viewModelScope.launch {
            sendSideEffect(SettingsSideEffect.NavigateToPrivacyPolicy)
        }
    }

    private fun navigateToTermsOfService() {
        viewModelScope.launch {
            sendSideEffect(SettingsSideEffect.NavigateToTermsOfService)
        }
    }
}
