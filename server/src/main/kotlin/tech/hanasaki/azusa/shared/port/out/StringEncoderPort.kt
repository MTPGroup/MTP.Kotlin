package tech.hanasaki.azusa.shared.port.out

interface StringEncoderPort {
    fun encode(rawToken: String): String
    fun verify(raw: String, encode: String): Boolean
}