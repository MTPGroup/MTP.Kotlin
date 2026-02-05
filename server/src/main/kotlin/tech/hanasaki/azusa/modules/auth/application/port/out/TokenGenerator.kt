package tech.hanasaki.azusa.modules.auth.application.port.out

import tech.hanasaki.azusa.common.domain.model.Email
import tech.hanasaki.azusa.common.domain.model.UserId
import tech.hanasaki.azusa.modules.auth.application.result.TokenPair


interface TokenGenerator {
    fun generate(userId: UserId, email: Email): TokenPair
}
