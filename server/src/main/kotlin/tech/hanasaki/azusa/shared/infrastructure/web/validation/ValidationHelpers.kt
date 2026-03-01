package tech.hanasaki.azusa.shared.infrastructure.web.validation

fun ValidationCollector.collectPage(value: String?, fieldName: String = "page"): Int? {
    if (value == null) return null
    if (value.isBlank()) {
        add(fieldName, "页码不能为空")
        return null
    }
    val page = value.toIntOrNull()
    if (page == null) {
        add(fieldName, "页码必须为数字")
        return null
    }
    if (page < 1) {
        add(fieldName, "页码必须大于等于1")
        return null
    }
    return page
}

fun ValidationCollector.collectLimit(value: String?, fieldName: String = "limit"): Int? {
    if (value == null) return null
    if (value.isBlank()) {
        add(fieldName, "每页数量不能为空")
        return null
    }
    val limit = value.toIntOrNull()
    if (limit == null) {
        add(fieldName, "每页数量必须为数字")
        return null
    }
    if (limit !in 1..100) {
        add(fieldName, "每页数量必须在1-100之间")
        return null
    }
    return limit
}
