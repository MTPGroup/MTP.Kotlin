package tech.hanasaki.momotalk_plus.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.core.domain.repository.UserRepository

class GetLoginStateUseCase(private val userRepository: UserRepository) {
    operator fun invoke(): Flow<String?> = userRepository.getLoginState()
}