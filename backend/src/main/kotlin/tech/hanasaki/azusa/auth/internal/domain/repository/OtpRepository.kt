package tech.hanasaki.azusa.auth.internal.domain.repository

import tech.hanasaki.azusa.auth.internal.domain.model.Email
import tech.hanasaki.azusa.auth.internal.domain.model.Otp
import tech.hanasaki.azusa.auth.internal.domain.model.OtpType

interface OtpRepository {
    fun save(otp: Otp)
    fun findValidLatest(email: Email, type: OtpType): Otp?
    fun markAsUsed(otp: Otp)
}