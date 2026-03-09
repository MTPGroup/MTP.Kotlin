package tech.hanasaki.azusa.shared.infrastructure.web.validation

import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import kotlin.uuid.Uuid

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

fun ValidationCollector.collectAuthorId(value: String?, fieldName: String = "authorId"): UserId? {
    return runCatching { UserId(Uuid.parse(value.orEmpty())) }.getOrNull()
}

fun ValidationCollector.collectVisibility(value: String?, fieldName: String = "visibility"): String? {
    if (value == null) return "all"
    if (value.isBlank() or !listOf("all", "public", "private").contains(value)) {
        add(fieldName, "可见性需为\"all\", \"public\"或\"private\"")
        return null
    }
    return value
}

fun ValidationCollector.collectScope(value: String?, fieldName: String = "scope"): String? {
    if (value == null) return null
    if (value.isBlank() or (value != "mine")) {
        add(fieldName, "scope目前只支持mine")
        return null
    }
    return value
}

fun ValidationCollector.collectionSort(value: String?, fieldName: String = "sort"): String? {
    if (value == null) return "newest"
    if (value.isBlank() or !listOf("newest", "popular", "name").contains(value)) {
        add(fieldName, "排序方式仅支持\"newest\", \"popular\"和\"name\"")
        return null
    }
    return value
}

fun ValidationCollector.collectionTags(value: String?, fieldName: String = "tags"): Set<String> {
    if (value == null) return emptySet()
    if (value.isBlank()) return emptySet()
    return value.split(",").map { it.trim() }.toSet()
}
