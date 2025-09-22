package tech.hanasaki.momotalk_plus.features.auth.data.repository

import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.features.auth.data.datasource.remote.AuthRemoteDatasource
import tech.hanasaki.momotalk_plus.features.auth.data.model.SignInWithPasswordResponse
import tech.hanasaki.momotalk_plus.features.auth.domain.model.AuthError
import tech.hanasaki.momotalk_plus.features.auth.domain.model.OTPType
import tech.hanasaki.momotalk_plus.features.auth.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val remoteDatasource: AuthRemoteDatasource,
) : AuthRepository {
    override suspend fun signUp(
        email: String,
        username: String,
        password: String
    ): Result<Unit, AuthError> =
        remoteDatasource.signUpWithPassword(
            username,
            email,
            password,
        ).map { }

    override suspend fun signInWithPassword(
        email: String,
        password: String,
    ): Result<SignInWithPasswordResponse, AuthError> =
        remoteDatasource.signInWithPassword(email, password)

    override suspend fun signOut(): Result<Unit, AuthError> =
        remoteDatasource.signOut().map { }

    override suspend fun sendEmailVerification(
        email: String,
        type: OTPType
    ): Result<Unit, AuthError> =
        remoteDatasource.sendEmailVerification(email, type).map { }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit, AuthError> =
        remoteDatasource.sendPasswordResetEmail(email).map { }

    override suspend fun verifyEmail(
        email: String,
        otp: String
    ): Result<Unit, AuthError> =
        remoteDatasource.verifyEmail(email, otp).map { }

    override suspend fun resetPassword(
        email: String,
        otp: String,
        password: String
    ): Result<Unit, AuthError> =
        remoteDatasource.resetPassword(email, otp, password).map { }
}
