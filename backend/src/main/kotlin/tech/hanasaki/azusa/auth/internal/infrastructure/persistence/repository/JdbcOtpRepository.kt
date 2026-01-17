package tech.hanasaki.azusa.auth.internal.infrastructure.persistence.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.stereotype.Repository
import tech.hanasaki.azusa.auth.internal.domain.model.Email
import tech.hanasaki.azusa.auth.internal.domain.model.Otp
import tech.hanasaki.azusa.auth.internal.domain.model.OtpType
import tech.hanasaki.azusa.auth.internal.domain.repository.OtpRepository
import tech.hanasaki.azusa.auth.internal.infrastructure.persistence.mapper.OtpEntityMapper
import java.time.Instant

@Repository
class JdbcOtpRepository(
    private val aggregateTemplate: JdbcAggregateTemplate,
    private val otpRepository: SpringDataOtpEntityRepository,
    private val mapper: OtpEntityMapper,
) : OtpRepository {
    override suspend fun save(otp: Otp): Unit = withContext(Dispatchers.IO) {
        val entity = mapper.toEntity(otp)
        if (otpRepository.existsById(otp.id)) {
            aggregateTemplate.save(entity)
        } else {
            aggregateTemplate.insert(entity)
        }
    }

    override suspend fun findValidLatest(email: Email, type: OtpType): Otp? = withContext(Dispatchers.IO) {
        val entity = otpRepository.findFirstByEmailAndTypeAndIsUsedFalseOrderByCreatedAtDesc(email.value, type.value)
        entity?.let {
            val domain = mapper.toDomain(it)
            if (domain.isValid()) domain else null
        }
    }

    override suspend fun markAsUsed(otp: Otp): Unit = withContext(Dispatchers.IO) {
        val entity = otpRepository.findById(otp.id).orElse(null) ?: return@withContext
        aggregateTemplate.save(entity.copy(isUsed = true, usedAt = Instant.now()))
    }
}
