package tech.hanasaki.azusa.auth.internal.domain.repository

import tech.hanasaki.azusa.auth.internal.domain.model.Email
import tech.hanasaki.azusa.auth.internal.domain.model.Otp
import tech.hanasaki.azusa.auth.internal.domain.model.OtpType

interface OtpRepository {
    suspend fun save(otp: Otp)
    suspend fun findValidLatest(email: Email, type: OtpType): Otp?
    suspend fun markAsUsed(otp: Otp)
}