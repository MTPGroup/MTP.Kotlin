package tech.hanasaki.momotalk_plus.core.domain.usecase

import tech.hanasaki.momotalk_plus.core.domain.repository.UserRepository

class SaveLoginStateUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(
        uid: String,
        idToken: String,
        refreshToken: String,
        expiresIn: Long,
    ) = userRepository.saveLoginState(uid, idToken, refreshToken, expiresIn)
}