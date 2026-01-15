package tech.hanasaki.azusa.modules.auth.domain.repository

import tech.hanasaki.azusa.modules.auth.domain.model.Email
import tech.hanasaki.azusa.modules.auth.domain.model.Otp
import tech.hanasaki.azusa.modules.auth.domain.model.OtpType

interface OtpRepository {
    suspend fun save(otp: Otp)
    suspend fun findValidLatest(email: Email, type: OtpType): Otp?
    suspend fun markAsUsed(otp: Otp)
}