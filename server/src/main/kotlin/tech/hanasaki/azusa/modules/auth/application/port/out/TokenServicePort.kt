package tech.hanasaki.azusa.modules.auth.application.port.out

import tech.hanasaki.azusa.modules.auth.application.dto.TokenPair
import tech.hanasaki.azusa.modules.auth.domain.model.UserRole
import tech.hanasaki.azusa.shared.domain.model.vo.Email
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

interface TokenServicePort {
    fun generate(userId: UserId, email: Email, role: UserRole): TokenPair
    fun verify(refreshToken: String): UserId
}