package tech.hanasaki.momotalk_plus.features.auth.domain.usecase

import tech.hanasaki.momotalk_plus.core.domain.model.AppError
import tech.hanasaki.momotalk_plus.core.domain.model.IResult
import tech.hanasaki.momotalk_plus.features.auth.domain.repository.AuthRepository

class SignUpUserUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        email: String,
        username: String,
        password: String,
    ): IResult<Unit, AppError> {
        if (email.isBlank()) {
            return IResult.Error(AppError("邮箱不能为空"))
        }
        if (username.isBlank()) {
            return IResult.Error(AppError("用户名不能为空"))
        }
        if (password.length < 8) {
            return IResult.Error(AppError("密码长度不能少于8位"))
        }
        return repository.signUp(email, username, password)
    }
}