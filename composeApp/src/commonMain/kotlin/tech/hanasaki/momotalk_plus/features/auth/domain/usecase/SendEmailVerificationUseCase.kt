package tech.hanasaki.momotalk_plus.features.auth.domain.usecase

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.features.auth.domain.model.AuthError
import tech.hanasaki.momotalk_plus.features.auth.domain.model.OTPType
import tech.hanasaki.momotalk_plus.features.auth.domain.repository.AuthRepository

class SendEmailVerificationUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, type: OTPType = OTPType.VERIFY_EMAIL): IResult<Unit, AppError> =
        repository.sendEmailVerification(email, type).mapError { error ->
            when (error) {
                is AuthError.NetworkError -> AppError(error.originalException.message ?: "网络错误")
                is AuthError.ApiError -> AppError("发送验证邮件失败")
                else -> AppError("未知错误")
            }
        }
}