package tech.hanasaki.azusa.modules.setting.adapter.`in`.event

import tech.hanasaki.azusa.modules.setting.application.port.`in`.SettingUseCasePort
import tech.hanasaki.azusa.shared.domain.event.UserRegisteredIntegrationEvent
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.shared.port.`in`.IntegrationEventHandlerPort

class UserRegisteredHandler(
    private val settingService: SettingUseCasePort,
) : IntegrationEventHandlerPort<UserRegisteredIntegrationEvent> {
    override suspend fun invoke(event: UserRegisteredIntegrationEvent) {
        settingService.createSetting(UserId(event.userId))
    }
}