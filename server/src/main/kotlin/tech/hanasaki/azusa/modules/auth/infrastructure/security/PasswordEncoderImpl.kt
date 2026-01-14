package tech.hanasaki.azusa.modules.auth.infrastructure.security

import at.favre.lib.crypto.bcrypt.BCrypt
import tech.hanasaki.azusa.modules.auth.application.service.PasswordEncoder

class PasswordEncoderImpl : PasswordEncoder {
    private val cost = 12

    override fun encode(raw: String): String =
        BCrypt.withDefaults().hashToString(cost, raw.toCharArray())

    override fun matches(raw: String, encoded: String): Boolean =
        BCrypt.verifyer().verify(raw.toCharArray(), encoded).verified
}