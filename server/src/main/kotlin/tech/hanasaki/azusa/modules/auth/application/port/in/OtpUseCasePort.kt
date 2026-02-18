package tech.hanasaki.azusa.modules.auth.application.port.`in`

import tech.hanasaki.azusa.modules.auth.domain.model.OtpType
import tech.hanasaki.azusa.shared.domain.model.vo.Email

interface OtpUseCasePort {
    suspend fun generate(email: Email, type: OtpType)
    suspend fun verify(email: Email, type: OtpType, code: String)
}