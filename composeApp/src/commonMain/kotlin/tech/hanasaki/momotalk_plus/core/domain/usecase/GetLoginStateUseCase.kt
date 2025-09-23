package tech.hanasaki.momotalk_plus.core.domain.usecase

import tech.hanasaki.momotalk_plus.core.domain.repository.UserRepository

class GetLoginStateUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(): Boolean = userRepository.getLoginState()
}