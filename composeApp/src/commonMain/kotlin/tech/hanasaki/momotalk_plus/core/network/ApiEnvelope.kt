package tech.hanasaki.momotalk_plus.core.network

import kotlinx.serialization.Serializable

@Serializable
data class ApiEnvelope<T>(
    val success: Boolean,
    val code: String? = null,
    val message: String,
    val data: T? = null,
    val errors: Map<String, String>? = null,
    val timestamp: String? = null,
)

@Serializable
data class PageDto<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
)

