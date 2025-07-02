package tech.hanasaki.momotalk_plus.features.login.data.repository

import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.features.login.data.datasource.remote.AuthRemoteDatasource
import tech.hanasaki.momotalk_plus.features.login.data.model.CaptchaIdResponse
import tech.hanasaki.momotalk_plus.features.login.data.model.CaptchaResponse
import tech.hanasaki.momotalk_plus.features.login.data.model.SignInWithPasswordResponse
import tech.hanasaki.momotalk_plus.features.login.domain.model.AuthError
import tech.hanasaki.momotalk_plus.features.login.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val remoteDatasource: AuthRemoteDatasource,
) : AuthRepository {
    override suspend fun signUp(
        email: String?,
        phoneNumber: String?,
        username: String,
        password: String,
        verificationToken: String,
    ): Result<Unit, AuthError> =
        remoteDatasource.signUpWithPassword(
            email,
            phoneNumber,
            username,
            password,
            verificationToken,
        ).map { }

    override suspend fun signInWithPassword(
        username: String,
        password: String,
    ): Result<SignInWithPasswordResponse, AuthError> =
        remoteDatasource.signInWithPassword(username, password)

    override suspend fun sendResetPasswordCode(
        email: String?,
        phoneNumber: String?,
        captchaId: String,
    ): Result<String, AuthError> =
        remoteDatasource.sendVerificationCode(email, phoneNumber, captchaId)


    override suspend fun verifyPasswordResetCode(
        verificationId: String,
        verificationCode: String,
    ): Result<String, AuthError> =
        remoteDatasource.getVerificationToken(verificationId, verificationCode)

    override suspend fun resetPassword(
        email: String?,
        phoneNumber: String?,
        newPassword: String,
        verificationToken: String,
    ): Result<Unit, AuthError> = remoteDatasource.resetPassword(
        email,
        phoneNumber,
        newPassword,
        verificationToken
    ).map { }

    override suspend fun getImageCaptcha(): Result<CaptchaResponse, AuthError> =
        remoteDatasource.getImageCaptcha()

    override suspend fun verifyImageCaptcha(
        captchaToken: String,
        captchaInput: String
    ): Result<CaptchaIdResponse, AuthError> =
        remoteDatasource.getCaptchaId(captchaToken, captchaInput)
}
