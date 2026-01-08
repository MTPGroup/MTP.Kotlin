package tech.hanasaki.momotalk_plus.features.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import tech.hanasaki.momotalk_plus.core.domain.usecase.*
import tech.hanasaki.momotalk_plus.core.theme.AppTheme
import tech.hanasaki.momotalk_plus.core.theme.ThemeManager
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
) : ViewModel(), ContainerHost<SettingsState, SettingsSideEffect> {

    override val container = viewModelScope.container<SettingsState, SettingsSideEffect>(SettingsState())

    init {
        loadSettings()
    }

    fun onIntent(intent: SettingsIntent) {
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

    private fun loadSettings() = intent {
        getUserSettingsUseCase()
            .catch { e ->
                postSideEffect(SettingsSideEffect.ShowMessage("加载设置失败: ${e.message}"))
            }
            .collect { userSettings ->
                val availableThemes = themeManager.getAvailableThemes()
                val savedTheme = availableThemes.find { it.id == userSettings.theme.name.lowercase() }
                    ?: availableThemes.first()

                themeManager.setTheme(savedTheme)

                reduce {
                    state.copy(
                        currentTheme = savedTheme,
                        availableThemes = availableThemes,
                        notificationsEnabled = userSettings.notificationsEnabled,
                        soundEnabled = userSettings.soundEnabled,
                        vibrationEnabled = userSettings.vibrationEnabled
                    )
                }
            }
    }

    private fun changeTheme(theme: AppTheme) = intent {
        saveThemeUseCase(theme.id)
        themeManager.setTheme(theme)
        reduce { state.copy(currentTheme = theme) }
        postSideEffect(SettingsSideEffect.ShowMessage("主题已切换"))
    }

    private fun toggleNotifications(enabled: Boolean) = intent {
        saveNotificationSettingsUseCase(enabled)
        reduce { state.copy(notificationsEnabled = enabled) }
    }

    private fun toggleSound(enabled: Boolean) = intent {
        saveSoundSettingsUseCase(enabled)
        reduce { state.copy(soundEnabled = enabled) }
    }

    private fun toggleVibration(enabled: Boolean) = intent {
        saveVibrationSettingsUseCase(enabled)
        reduce { state.copy(vibrationEnabled = enabled) }
    }

    private fun navigateToAbout() = intent {
        postSideEffect(SettingsSideEffect.NavigateToAbout)
    }

    private fun navigateToPrivacyPolicy() = intent {
        postSideEffect(SettingsSideEffect.NavigateToPrivacyPolicy)
    }

    private fun navigateToTermsOfService() = intent {
        postSideEffect(SettingsSideEffect.NavigateToTermsOfService)
    }
}
