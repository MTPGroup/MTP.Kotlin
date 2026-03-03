@file:OptIn(kotlin.time.ExperimentalTime::class)

package tech.hanasaki.momotalk_plus.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.core.domain.model.User
import tech.hanasaki.momotalk_plus.core.domain.repository.SessionRepository

/**
 * 观察当前用户信息 UseCase
 *
 * 返回一个 Flow，当用户信息变化时自动推送更新
 */
class ObserveCurrentUserUseCase(
    private val repository: SessionRepository,
) {
    operator fun invoke(): Flow<User?> =
        repository.observeUser()
}

