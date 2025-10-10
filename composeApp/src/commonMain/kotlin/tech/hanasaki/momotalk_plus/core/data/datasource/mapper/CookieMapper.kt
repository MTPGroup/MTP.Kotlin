package tech.hanasaki.momotalk_plus.core.data.datasource.mapper

import tech.hanasaki.momotalk_plus.core.data.datasource.local.entity.CookieEntity
import tech.hanasaki.momotalk_plus.core.domain.model.SerializableCookie

/**
 * 将 CookieEntity 转换为 SerializableCookie
 */
object CookieMapper {
    fun CookieEntity.toDomain(): SerializableCookie =
        SerializableCookie(
            name = name,
            value = value,
            expires = expires,
            maxAge = maxAge,
            domain = domain,
            path = path,
            secure = secure,
            httpOnly = httpOnly,
        )

    fun SerializableCookie.toEntity(): CookieEntity =
        CookieEntity(
            name = name,
            value = value,
            expires = expires ?: "",
            maxAge = maxAge ?: 0,
            domain = domain ?: "",
            path = path ?: "",
            secure = secure,
            httpOnly = httpOnly,
            extensions = "",
        )
}