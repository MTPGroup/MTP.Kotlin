package tech.hanasaki.azusa.auth.domain.repository

import tech.hanasaki.azusa.auth.domain.model.Email
import tech.hanasaki.azusa.auth.domain.model.Otp
import tech.hanasaki.azusa.auth.domain.model.OtpType

interface OtpRepository {
    fun save(otp: Otp)
    fun findValidLatest(email: Email, type: OtpType): Otp?
    fun markAsUsed(otp: Otp)
}