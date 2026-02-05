package tech.hanasaki.azusa.modules.auth.application.port.`in`

import tech.hanasaki.azusa.common.domain.model.UserId

interface TokenVerifier {
    fun verify(refreshToken: String): UserId
}
