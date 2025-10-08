package tech.hanasaki.momotalk_plus.features.auth.domain.usecase

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.features.auth.domain.repository.AuthRepository

class VerifyEmailUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, otp: String): IResult<Unit, AppError> =
        repository.verifyEmail(email, otp)
}