package tech.hanasaki.momotalk_plus.features.auth.domain.usecase

import tech.hanasaki.momotalk_plus.core.domain.model.AppError
import tech.hanasaki.momotalk_plus.core.domain.model.IResult
import tech.hanasaki.momotalk_plus.features.auth.domain.model.OTPType
import tech.hanasaki.momotalk_plus.features.auth.domain.repository.AuthRepository

class SendEmailVerificationUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, type: OTPType = OTPType.VERIFY_EMAIL): IResult<Unit, AppError> =
        repository.sendEmailVerification(email, type)
}