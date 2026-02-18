package tech.hanasaki.azusa.shared.infrastructure.security

import tech.hanasaki.azusa.shared.port.out.StringEncoderPort
import java.security.MessageDigest

class StringEncoder : StringEncoderPort {
    override fun encode(rawToken: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(rawToken.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    override fun verify(raw: String, encode: String): Boolean {
        return encode(raw) == encode
    }
}