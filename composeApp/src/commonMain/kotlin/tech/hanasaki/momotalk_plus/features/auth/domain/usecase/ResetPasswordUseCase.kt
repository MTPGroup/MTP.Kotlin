package tech.hanasaki.momotalk_plus.features.auth.domain.usecase

import io.github.jan.supabase.auth.OtpType
import tech.hanasaki.momotalk_plus.features.auth.domain.repository.AuthRepository

class ResetPasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, otp: String, newPassword: String): Result<Unit> {
        return try {
            if (newPassword.length < 8) {
                return Result.failure(Exception("密码长度至少为8位"))
            }

            repository.verifyEmail(OtpType.Email.RECOVERY, email, otp)
            repository.resetPassword(email, newPassword)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}