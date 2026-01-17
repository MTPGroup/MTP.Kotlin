package tech.hanasaki.azusa.auth.internal.infrastructure.persistence.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import tech.hanasaki.azusa.auth.internal.infrastructure.persistence.entity.OtpEntity
import tech.hanasaki.azusa.auth.internal.infrastructure.persistence.entity.RefreshTokenEntity
import tech.hanasaki.azusa.auth.internal.infrastructure.persistence.entity.UserEntity
import java.util.*

@Repository
interface SpringDataUserEntityRepository : CrudRepository<UserEntity, UUID> {
    fun findByEmail(email: String): UserEntity?
}

@Repository
interface SpringDataOtpEntityRepository : CrudRepository<OtpEntity, UUID> {
    fun findFirstByEmailAndTypeAndIsUsedFalseOrderByCreatedAtDesc(email: String, type: String): OtpEntity?
}

@Repository
interface SpringDataRefreshTokenEntityRepository : CrudRepository<RefreshTokenEntity, UUID> {
    fun findByTokenHash(tokenHash: String): RefreshTokenEntity?
    fun findAllByUserId(userId: UUID): List<RefreshTokenEntity>
}
