package tech.hanasaki.azusa.auth.infrastructure.security

import at.favre.lib.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Service
import tech.hanasaki.azusa.auth.application.service.PasswordEncoder

@Service
class PasswordEncoderImpl : PasswordEncoder {
    private val cost = 12

    override fun encode(raw: String): String =
        BCrypt.withDefaults().hashToString(cost, raw.toCharArray())

    override fun matches(raw: String, encoded: String): Boolean =
        BCrypt.verifyer().verify(raw.toCharArray(), encoded).verified
}
