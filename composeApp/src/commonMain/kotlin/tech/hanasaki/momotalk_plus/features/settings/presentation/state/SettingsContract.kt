package tech.hanasaki.momotalk_plus.features.settings.presentation.state

import tech.hanasaki.momotalk_plus.core.theme.AppTheme

data class SettingsState(
    val currentTheme: AppTheme? = null,
    val availableThemes: List<AppTheme> = emptyList(),
    val notificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
)

sealed class SettingsIntent {
    data class ThemeChanged(val theme: AppTheme) : SettingsIntent()
    data class NotificationsToggled(val enabled: Boolean) : SettingsIntent()
    data class SoundToggled(val enabled: Boolean) : SettingsIntent()
    data class VibrationToggled(val enabled: Boolean) : SettingsIntent()
    data object LoadSettings : SettingsIntent()
    data object AboutClicked : SettingsIntent()
    data object PrivacyPolicyClicked : SettingsIntent()
    data object TermsOfServiceClicked : SettingsIntent()
}

sealed class SettingsSideEffect {
    data object NavigateToAbout : SettingsSideEffect()
    data object NavigateToPrivacyPolicy : SettingsSideEffect()
    data object NavigateToTermsOfService : SettingsSideEffect()
    data class ShowMessage(val message: String) : SettingsSideEffect()
}

