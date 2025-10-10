package tech.hanasaki.momotalk_plus.core.data.datasource.mapper

import tech.hanasaki.momotalk_plus.core.data.datasource.local.entity.SessionEntity
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.dto.SessionDto
import tech.hanasaki.momotalk_plus.core.domain.model.Session
import kotlin.time.ExperimentalTime

object SessionMapper {
    @OptIn(ExperimentalTime::class)
    fun SessionDto.toSession(): Session {
        return Session(
            id = id,
            userId = userId,
            token = token,
            expiresAt = expiresAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
            ipAddress = ipAddress,
            userAgent = userAgent,
        )
    }

    @OptIn(ExperimentalTime::class)
    fun SessionEntity.toSession(): Session {
        return Session(
            id = id,
            token = token,
            expiresAt = expiresAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
            ipAddress = ipAddress,
            userAgent = userAgent,
            userId = userId
        )
    }

    @OptIn(ExperimentalTime::class)
    fun Session.toSessionEntity(): SessionEntity {
        return SessionEntity(
            id = id,
            token = token,
            expiresAt = expiresAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
            ipAddress = ipAddress,
            userAgent = userAgent,
            userId = userId
        )
    }
}
