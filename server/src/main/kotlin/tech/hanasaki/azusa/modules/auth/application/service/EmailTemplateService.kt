package tech.hanasaki.azusa.modules.auth.application.service

import tech.hanasaki.azusa.modules.auth.domain.model.OtpType

object EmailTemplateService {
    private val templateCache = mutableMapOf<String, String>()

    fun renderOtpEmail(type: OtpType, code: String, expireMinutes: Int = 10): String {
        val template = loadTemplate("otp-verification.html")

        val (title, description) = when (type) {
            OtpType.VERIFY_EMAIL -> Pair(
                "验证您的邮箱",
                "感谢您注册 Azusa！请使用以下验证码完成邮箱验证："
            )

            OtpType.RESET_PASSWORD -> Pair(
                "重置密码",
                "您正在重置密码，请使用以下验证码完成操作："
            )

            OtpType.SIGN_IN -> Pair(
                "登录验证",
                "您正在登录 Azusa，请使用以下验证码完成登录："
            )
        }

        return template
            .replace("{{title}}", title)
            .replace("{{description}}", description)
            .replace("{{code}}", code)
            .replace("{{expireMinutes}}", expireMinutes.toString())
    }

    private fun loadTemplate(name: String): String {
        return templateCache.getOrPut(name) {
            val resource = this::class.java.classLoader.getResource("templates/email/$name")
                ?: throw IllegalStateException("Email template not found: $name")
            resource.readText()
        }
    }
}