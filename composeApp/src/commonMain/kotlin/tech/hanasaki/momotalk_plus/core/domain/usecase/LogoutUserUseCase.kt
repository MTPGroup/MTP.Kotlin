package tech.hanasaki.momotalk_plus.core.domain.usecase

import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.core.domain.model.UserError
import tech.hanasaki.momotalk_plus.core.domain.repository.UserRepository

class LogoutUserUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(): Result<Unit, UserError> = userRepository.logout()
}