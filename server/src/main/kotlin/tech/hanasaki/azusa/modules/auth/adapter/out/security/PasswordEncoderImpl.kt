package tech.hanasaki.azusa.modules.auth.adapter.out.security

import at.favre.lib.crypto.bcrypt.BCrypt
import tech.hanasaki.azusa.modules.auth.application.port.out.PasswordEncoder

class PasswordEncoderImpl : PasswordEncoder {
    private val cost = 12

    override fun encode(raw: String): String =
        BCrypt.withDefaults().hashToString(cost, raw.toCharArray())

    override fun matches(raw: String, encoded: String): Boolean =
        BCrypt.verifyer().verify(raw.toCharArray(), encoded).verified
}
