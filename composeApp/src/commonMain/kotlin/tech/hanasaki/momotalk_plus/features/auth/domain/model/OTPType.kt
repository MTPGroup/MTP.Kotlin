package tech.hanasaki.momotalk_plus.features.auth.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class OTPType {
    @SerialName("sign_in")
    SIGN_IN,

    @SerialName("reset_password")
    RESET_PASSWORD,

    @SerialName("email_verification")
    VERIFY_EMAIL,
}