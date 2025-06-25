package tech.hanasaki.momotalk_plus.features.login.data.repository

import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.core.domain.model.User
import tech.hanasaki.momotalk_plus.features.login.data.datasource.remote.AuthRemoteDatasource
import tech.hanasaki.momotalk_plus.features.login.domain.model.AuthError
import tech.hanasaki.momotalk_plus.features.login.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val remoteDatasource: AuthRemoteDatasource
) : AuthRepository {
    override suspend fun signUpWithEmailPassword(
        email: String,
        password: String
    ): Result<Unit, AuthError> =
        remoteDatasource.signUpWithPassword(email, password).map { }

    override suspend fun signInWithEmailPassword(
        email: String,
        password: String
    ): Result<Unit, AuthError> =
        remoteDatasource.signInWithPassword(email, password).map { }

    override suspend fun sendResetPasswordEmail(email: String): Result<Unit, AuthError> =
        remoteDatasource.sendResetPasswordEmail(email).map { }


    override suspend fun verifyPasswordResetCode(oobCode: String): Result<Unit, AuthError> =
        remoteDatasource.verifyResetPasswordCode(oobCode).map { }

    override suspend fun resetPassword(
        oobCode: String,
        newPassword: String
    ): Result<Unit, AuthError> = remoteDatasource.resetPassword(oobCode, newPassword).map { }

    override fun isUserLoggedIn(): Boolean? {
        TODO("Not yet implemented")
    }

    override fun getAuthStateFlow(): Flow<User?> {
        TODO("Not yet implemented")
    }
}
