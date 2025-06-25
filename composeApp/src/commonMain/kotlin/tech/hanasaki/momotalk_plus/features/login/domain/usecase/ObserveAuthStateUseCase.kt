package tech.hanasaki.momotalk_plus.features.login.domain.usecase

import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.core.domain.model.User
import tech.hanasaki.momotalk_plus.features.login.domain.repository.AuthRepository

class ObserveAuthStateUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(): Flow<User?> = authRepository.getAuthStateFlow()
}