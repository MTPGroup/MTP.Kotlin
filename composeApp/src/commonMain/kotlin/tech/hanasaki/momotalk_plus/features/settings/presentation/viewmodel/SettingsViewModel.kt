package tech.hanasaki.momotalk_plus.features.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import tech.hanasaki.momotalk_plus.features.settings.domain.usecase.ObserveSettingsUseCase
import tech.hanasaki.momotalk_plus.features.settings.domain.usecase.SaveNotificationsUseCase
import tech.hanasaki.momotalk_plus.features.settings.domain.usecase.SaveSoundUseCase
import tech.hanasaki.momotalk_plus.features.settings.domain.usecase.SaveThemeUseCase
import tech.hanasaki.momotalk_plus.features.settings.domain.usecase.SaveVibrationUseCase
import tech.hanasaki.momotalk_plus.core.theme.AppTheme
import tech.hanasaki.momotalk_plus.core.theme.PredefinedThemes
import tech.hanasaki.momotalk_plus.core.theme.ThemeManager
import tech.hanasaki.momotalk_plus.features.settings.presentation.state.SettingsIntent
import tech.hanasaki.momotalk_plus.features.settings.presentation.state.SettingsSideEffect
import tech.hanasaki.momotalk_plus.features.settings.presentation.state.SettingsState

class SettingsViewModel(
    private val themeManager: ThemeManager,
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val saveThemeUseCase: SaveThemeUseCase,
    private val saveNotificationsUseCase: SaveNotificationsUseCase,
    private val saveSoundUseCase: SaveSoundUseCase,
    private val saveVibrationUseCase: SaveVibrationUseCase,
) : ViewModel(), ContainerHost<SettingsState, SettingsSideEffect> {

    override val container = viewModelScope.container<SettingsState, SettingsSideEffect>(SettingsState())

    private val supportedThemes: List<AppTheme> = listOf(
        PredefinedThemes.Default.copy(id = "system", name = "跟随系统"),
        PredefinedThemes.Default.copy(id = "light", name = "浅色"),
        PredefinedThemes.Dark.copy(id = "dark", name = "深色"),
    )

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
        observeSettingsUseCase()
            .catch { e ->
                postSideEffect(SettingsSideEffect.ShowMessage("加载设置失败: ${e.message}"))
            }
            .collect { userSettings ->
                val savedTheme = supportedThemes.find { it.id == userSettings.themeId }
                    ?: supportedThemes.first()

                themeManager.setTheme(savedTheme)

                reduce {
                    state.copy(
                        currentTheme = savedTheme,
                        availableThemes = supportedThemes,
                        notificationsEnabled = userSettings.notificationsEnabled,
                        soundEnabled = userSettings.soundEnabled,
                        vibrationEnabled = userSettings.vibrationEnabled,
                    )
                }
            }
    }

    private fun changeTheme(theme: AppTheme) = intent {
        saveThemeUseCase(theme.id)
            .onSuccess {
                themeManager.setTheme(theme)
                reduce { state.copy(currentTheme = theme) }
                postSideEffect(SettingsSideEffect.ShowMessage("主题已切换"))
            }
            .onFailure {
                val previous = state.currentTheme ?: supportedThemes.first()
                themeManager.setTheme(previous)
                postSideEffect(SettingsSideEffect.ShowMessage("主题切换失败: ${it.message}"))
            }
    }

    private fun toggleNotifications(enabled: Boolean) = intent {
        saveNotificationsUseCase(enabled)
            .onSuccess { reduce { state.copy(notificationsEnabled = enabled) } }
            .onFailure { postSideEffect(SettingsSideEffect.ShowMessage("通知设置保存失败: ${it.message}")) }
    }

    private fun toggleSound(enabled: Boolean) = intent {
        saveSoundUseCase(enabled)
            .onSuccess { reduce { state.copy(soundEnabled = enabled) } }
            .onFailure { postSideEffect(SettingsSideEffect.ShowMessage("提示音设置保存失败: ${it.message}")) }
    }

    private fun toggleVibration(enabled: Boolean) = intent {
        saveVibrationUseCase(enabled)
            .onSuccess { reduce { state.copy(vibrationEnabled = enabled) } }
            .onFailure { postSideEffect(SettingsSideEffect.ShowMessage("振动设置保存失败: ${it.message}")) }
    }

    private fun navigateToAbout() = intent { postSideEffect(SettingsSideEffect.NavigateToAbout) }

    private fun navigateToPrivacyPolicy() = intent { postSideEffect(SettingsSideEffect.NavigateToPrivacyPolicy) }

    private fun navigateToTermsOfService() = intent { postSideEffect(SettingsSideEffect.NavigateToTermsOfService) }
}
