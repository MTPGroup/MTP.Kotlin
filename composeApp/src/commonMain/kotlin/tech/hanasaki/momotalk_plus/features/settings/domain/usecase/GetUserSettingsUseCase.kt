package tech.hanasaki.momotalk_plus.features.settings.domain.usecase

import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.features.settings.domain.model.UserSettings
import tech.hanasaki.momotalk_plus.features.settings.domain.repository.SettingsRepository

class GetUserSettingsUseCase(
    private val repository: SettingsRepository,
) {
    operator fun invoke(): Flow<UserSettings> {
        return repository.observeUserSettings()
    }
}
