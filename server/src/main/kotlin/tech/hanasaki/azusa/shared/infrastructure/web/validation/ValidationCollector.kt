package tech.hanasaki.azusa.shared.infrastructure.web.validation

import tech.hanasaki.azusa.shared.domain.exception.ValidationException

class ValidationCollector {
    private val errors = linkedMapOf<String, String>()

    fun add(field: String, message: String) {
        errors[field] = message
    }

    fun <T> vo(field: String, builder: () -> T): T? {
        return try {
            builder()
        } catch (e: IllegalArgumentException) {
            errors[field] = e.message ?: "参数不合法"
            null
        }
    }

    fun throwIfAny() {
        if (errors.isNotEmpty()) {
            throw ValidationException(
                message = "验证失败",
                details = errors.toMap(),
            )
        }
    }
}
