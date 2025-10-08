package tech.hanasaki.momotalk_plus.features.auth.domain.usecase

import tech.hanasaki.momotalk_plus.core.domain.model.AppError
import tech.hanasaki.momotalk_plus.core.domain.model.IResult
import tech.hanasaki.momotalk_plus.features.auth.domain.repository.AuthRepository

class SignOutUserUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): IResult<Unit, AppError> =
        repository.signOut()
}