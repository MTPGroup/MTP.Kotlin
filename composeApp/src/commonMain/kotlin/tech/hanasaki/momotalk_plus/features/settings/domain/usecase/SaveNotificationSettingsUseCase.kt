package tech.hanasaki.momotalk_plus.features.settings.domain.usecase

import tech.hanasaki.momotalk_plus.features.settings.domain.repository.SettingsRepository

class SaveNotificationSettingsUseCase(
    private val repository: SettingsRepository,
) {
    operator fun invoke(enabled: Boolean) {
        repository.saveNotificationsEnabled(enabled)
    }
}

