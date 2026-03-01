package tech.hanasaki.momotalk_plus.features.auth.domain.usecase

import tech.hanasaki.momotalk_plus.features.auth.domain.model.OTPType
import tech.hanasaki.momotalk_plus.features.auth.domain.repository.AuthRepository

class SendEmailVerificationUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, type: OTPType): Result<Unit> = try {
        repository.sendEmailVerification(email, type)
        Result.success(Unit)
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }
}