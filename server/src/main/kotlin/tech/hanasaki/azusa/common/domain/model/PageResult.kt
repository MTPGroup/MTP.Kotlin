package tech.hanasaki.azusa.common.domain.model

/**
 * 分页结果
 */
data class PageResult<T>(
    val items: List<T>,
    val total: Long,
    val page: Int,
    val limit: Int,
) {
    val totalPages: Int
        get() = if (limit > 0) ((total + limit - 1) / limit).toInt() else 0

    val hasNext: Boolean
        get() = page < totalPages

    val hasPrevious: Boolean
        get() = page > 1
}
