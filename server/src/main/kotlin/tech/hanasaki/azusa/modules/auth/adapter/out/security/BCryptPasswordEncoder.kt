package tech.hanasaki.azusa.modules.auth.adapter.out.security

import at.favre.lib.crypto.bcrypt.BCrypt
import tech.hanasaki.azusa.modules.auth.application.port.out.PasswordEncoderPort
import tech.hanasaki.azusa.modules.auth.domain.model.HashedPassword
import tech.hanasaki.azusa.modules.auth.domain.model.PlainPassword

class BCryptPasswordEncoder : PasswordEncoderPort {
    private val cost = 12

    override fun encode(raw: PlainPassword): HashedPassword {
        val hashedPassword = BCrypt.withDefaults().hashToString(cost, raw.value.toCharArray())
        return HashedPassword(hashedPassword)
    }

    override fun matches(raw: PlainPassword, encoded: HashedPassword): Boolean =
        BCrypt.verifyer().verify(raw.value.toCharArray(), encoded.value).verified
}
