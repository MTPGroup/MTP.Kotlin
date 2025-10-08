package tech.hanasaki.momotalk_plus.features.auth.domain.usecase

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.features.auth.domain.repository.AuthRepository

class SignInUserUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): IResult<String, AppError> {
        if (email == "" || password == "") {
            return IResult.Error(AppError("邮箱或密码不能为空"))
        }
        return repository.signInWithPassword(email, password)
            .map { signInWithPasswordResponse ->
                signInWithPasswordResponse.user.id
            }
    }
}