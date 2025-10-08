package tech.hanasaki.momotalk_plus.core.domain.usecase

import tech.hanasaki.momotalk_plus.core.domain.model.AppError
import tech.hanasaki.momotalk_plus.core.domain.model.IResult
import tech.hanasaki.momotalk_plus.core.domain.repository.UserRepository

class LogoutUserUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(): IResult<Unit, AppError> = userRepository.logout()
}