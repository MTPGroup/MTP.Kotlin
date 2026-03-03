package tech.hanasaki.momotalk_plus.features.settings.domain.usecase

import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.features.settings.domain.model.SettingsPreferences
import tech.hanasaki.momotalk_plus.features.settings.domain.repository.SettingsRepository

class ObserveSettingsUseCase(
    private val repository: SettingsRepository,
) {
    operator fun invoke(): Flow<SettingsPreferences> = repository.observeSettings()
}
