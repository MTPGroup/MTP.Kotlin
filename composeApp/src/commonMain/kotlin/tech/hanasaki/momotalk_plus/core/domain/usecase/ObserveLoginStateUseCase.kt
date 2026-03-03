package tech.hanasaki.momotalk_plus.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.core.domain.repository.SessionRepository

/**
 * 观察登录状态 UseCase (响应式)
 *
 * 返回一个 Flow，当登录状态变化时自动推送更新
 */
class ObserveLoginStateUseCase(
    private val repository: SessionRepository,
) {
    operator fun invoke(): Flow<Boolean> {
        return repository.observeLoginState()
    }
}

