package tech.hanasaki.momotalk_plus.features.auth.domain.usecase

import tech.hanasaki.momotalk_plus.core.domain.model.AppError
import tech.hanasaki.momotalk_plus.core.domain.model.IResult
import tech.hanasaki.momotalk_plus.features.auth.domain.repository.AuthRepository

class SendPasswordResetEmailUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): IResult<Unit, AppError> =
        repository.sendPasswordResetEmail(email)
}