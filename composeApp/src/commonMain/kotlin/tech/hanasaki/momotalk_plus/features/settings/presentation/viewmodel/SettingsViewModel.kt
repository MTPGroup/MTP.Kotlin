package tech.hanasaki.momotalk_plus.features.settings.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.BaseViewModel
import tech.hanasaki.momotalk_plus.core.theme.ThemeManager
import tech.hanasaki.momotalk_plus.features.settings.presentation.state.SettingsIntent
import tech.hanasaki.momotalk_plus.features.settings.presentation.state.SettingsSideEffect
import tech.hanasaki.momotalk_plus.features.settings.presentation.state.SettingsState

class SettingsViewModel(
    private val themeManager: ThemeManager,
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
            val currentTheme = themeManager.currentTheme.value
            val availableThemes = themeManager.getAvailableThemes()

            updateState {
                it.copy(
                    currentTheme = currentTheme,
                    availableThemes = availableThemes
                )
            }
        }
    }

    private fun changeTheme(theme: tech.hanasaki.momotalk_plus.core.theme.AppTheme) {
        viewModelScope.launch {
            themeManager.setTheme(theme)
            updateState { it.copy(currentTheme = theme) }
            sendSideEffect(SettingsSideEffect.ShowMessage("主题已切换"))
        }
    }

    private fun toggleNotifications(enabled: Boolean) {
        updateState { it.copy(notificationsEnabled = enabled) }
        // TODO: Save to preferences
    }

    private fun toggleSound(enabled: Boolean) {
        updateState { it.copy(soundEnabled = enabled) }
        // TODO: Save to preferences
    }

    private fun toggleVibration(enabled: Boolean) {
        updateState { it.copy(vibrationEnabled = enabled) }
        // TODO: Save to preferences
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

