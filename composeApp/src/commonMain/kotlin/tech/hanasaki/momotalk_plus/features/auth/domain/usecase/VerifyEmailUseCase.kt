package tech.hanasaki.momotalk_plus.features.auth.domain.usecase

import tech.hanasaki.momotalk_plus.features.auth.domain.model.OTPType
import tech.hanasaki.momotalk_plus.features.auth.domain.repository.AuthRepository

class VerifyEmailUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(type: OTPType, email: String, otp: String): Result<Unit> = try {
        repository.verifyEmail(type, email, otp)
        Result.success(Unit)
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }
}