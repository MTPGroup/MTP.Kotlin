package tech.hanasaki.momotalk_plus.features.auth.data.repository

import tech.hanasaki.momotalk_plus.core.domain.model.AppError
import tech.hanasaki.momotalk_plus.core.domain.model.IResult
import tech.hanasaki.momotalk_plus.features.auth.data.datasource.remote.AuthRemoteDatasource
import tech.hanasaki.momotalk_plus.features.auth.data.model.SignInWithPasswordResponse
import tech.hanasaki.momotalk_plus.features.auth.domain.model.OTPType
import tech.hanasaki.momotalk_plus.features.auth.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val remoteDatasource: AuthRemoteDatasource,
) : AuthRepository {
    override suspend fun signUp(
        email: String,
        username: String,
        password: String,
    ): IResult<Unit, AppError> =
        remoteDatasource.signUpWithPassword(
            username,
            email,
            password,
        ).map { }

    override suspend fun signInWithPassword(
        email: String,
        password: String,
    ): IResult<SignInWithPasswordResponse, AppError> =
        remoteDatasource.signInWithPassword(email, password)

    override suspend fun signOut(): IResult<Unit, AppError> =
        remoteDatasource.signOut().map { }

    override suspend fun sendEmailVerification(
        email: String,
        type: OTPType,
    ): IResult<Unit, AppError> =
        remoteDatasource.sendEmailVerification(email, type).map { }

    override suspend fun sendPasswordResetEmail(email: String): IResult<Unit, AppError> =
        remoteDatasource.sendPasswordResetEmail(email).map { }

    override suspend fun verifyEmail(
        email: String,
        otp: String,
    ): IResult<Unit, AppError> =
        remoteDatasource.verifyEmail(email, otp).map { }

    override suspend fun resetPassword(
        email: String,
        otp: String,
        password: String,
    ): IResult<Unit, AppError> =
        remoteDatasource.resetPassword(email, otp, password).map { }
}
