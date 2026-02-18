package tech.hanasaki.azusa.shared.infrastructure.web.validation

import tech.hanasaki.azusa.shared.domain.exception.ValidationException

fun validateEmail(email: String, fieldName: String = "email"): String {
    val errors = mutableMapOf<String, String>()

    if (email.isBlank()) {
        errors[fieldName] = "邮箱不能为空"
    } else if (!email.contains("@") || !email.contains(".")) {
        errors[fieldName] = "邮箱格式不正确"
    }

    if (errors.isNotEmpty()) {
        throw ValidationException(details = errors)
    }

    return email
}

fun validatePassword(password: String, fieldName: String = "password", minLength: Int = 6): String {
    val errors = mutableMapOf<String, String>()

    if (password.isBlank()) {
        errors[fieldName] = "密码不能为空"
    } else if (password.length < minLength) {
        errors[fieldName] = "密码至少需要 $minLength 个字符"
    }

    if (errors.isNotEmpty()) {
        throw ValidationException(details = errors)
    }

    return password
}

fun validateUsername(username: String, fieldName: String = "username"): String {
    val errors = mutableMapOf<String, String>()

    if (username.isBlank()) {
        errors[fieldName] = "用户名不能为空"
    } else if (username.length < 2) {
        errors[fieldName] = "用户名至少需要2个字符"
    } else if (username.length > 50) {
        errors[fieldName] = "用户名不能超过50个字符"
    }

    if (errors.isNotEmpty()) {
        throw ValidationException(details = errors)
    }

    return username
}

fun validatePage(value: String?, fieldName: String = "page"): Int {
    val errors = mutableMapOf<String, String>()

    if (value.isNullOrBlank()) {
        errors[fieldName] = "页码不能为空"
    } else {
        val page = value.toIntOrNull()
        if (page == null) errors[fieldName] = "页码必须为数字"
        else if (page < 1) errors[fieldName] = "页码必须大于等于1"
    }

    if (errors.isNotEmpty()) {
        throw ValidationException(details = errors)
    }

    return value?.toIntOrNull() ?: 1
}

fun validateLimit(value: String?): Int {
    val errors = mutableMapOf<String, String>()

    if (value.isNullOrBlank()) {
        errors["limit"] = "每页数量不能为空"
    } else {
        val limit = value.toIntOrNull()
        if (limit == null) errors["limit"] = "每页数量必须为数字"
        else if (limit !in 1..100) errors["limit"] = "每页数量必须在1-100之间"
    }

    if (errors.isNotEmpty()) {
        throw ValidationException(details = errors)
    }

    return value?.toIntOrNull() ?: 10
}


fun <T> validate(fieldName: String, value: T, validators: List<(T) -> String>): T {
    val errors = mutableMapOf<String, String>()

    validators.forEach { validator ->
        try {
            validator(value)
        } catch (e: ValidationException) {
            errors.putAll(e.details)
        }
    }

    if (errors.isNotEmpty()) {
        throw ValidationException(details = errors)
    }

    return value
}