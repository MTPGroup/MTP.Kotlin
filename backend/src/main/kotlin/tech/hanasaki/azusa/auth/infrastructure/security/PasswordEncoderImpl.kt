package tech.hanasaki.azusa.auth.infrastructure.security

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import tech.hanasaki.azusa.auth.application.port.PasswordEncoder

@Service
class PasswordEncoderImpl : PasswordEncoder {
    private val passwordEncoder = BCryptPasswordEncoder(12)

    override fun encode(raw: String): String =
        passwordEncoder.encode(raw)

    override fun matches(raw: String, encoded: String): Boolean =
        passwordEncoder.matches(raw, encoded)
}
