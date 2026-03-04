package tech.hanasaki.momotalk_plus.core.domain.usecase

import tech.hanasaki.momotalk_plus.core.domain.repository.SessionRepository

class RefreshCurrentUserUseCase(
    private val repository: SessionRepository,
) {
    suspend operator fun invoke() {
        repository.refreshCurrentUser()
    }
}
