package tech.hanasaki.momotalk_plus.features.auth.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import tech.hanasaki.momotalk_plus.features.auth.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val supabase: SupabaseClient,
) : AuthRepository {
    override suspend fun signUp(email: String, password: String) {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signInWithPassword(
        email: String,
        password: String,
    ) {
        supabase.auth.signInWith(
            Email,
        ) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signOut() =
        supabase.auth.signOut()

    override suspend fun sendEmailVerification(
        email: String,
        type: OtpType.Email,
    ) {
        supabase.auth.resendEmail(type, email)
    }


    override suspend fun verifyEmail(type: OtpType.Email, email: String, otp: String) {
        supabase.auth.verifyEmailOtp(type, email = email, token = otp)
    }

    override suspend fun sendResetPasswordEmail(email: String) {
        supabase.auth.resetPasswordForEmail(email)
    }

    override suspend fun resetPassword(email: String, password: String) {
        supabase.auth.updateUser {
            this.password = password
        }
    }
}
