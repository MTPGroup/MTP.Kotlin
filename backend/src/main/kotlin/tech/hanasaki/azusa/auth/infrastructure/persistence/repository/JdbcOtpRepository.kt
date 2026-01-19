package tech.hanasaki.azusa.auth.infrastructure.persistence.repository

import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.stereotype.Repository
import tech.hanasaki.azusa.auth.domain.model.Email
import tech.hanasaki.azusa.auth.domain.model.Otp
import tech.hanasaki.azusa.auth.domain.model.OtpType
import tech.hanasaki.azusa.auth.domain.repository.OtpRepository
import tech.hanasaki.azusa.auth.infrastructure.persistence.mapper.OtpEntityMapper
import java.time.Instant

@Repository
class JdbcOtpRepository(
    private val aggregateTemplate: JdbcAggregateTemplate,
    private val otpRepository: SpringDataOtpEntityRepository,
    private val mapper: OtpEntityMapper,
) : OtpRepository {
    override fun save(otp: Otp) {
        val exists = otpRepository.existsById(otp.id)
        val entity = mapper.toEntity(otp, isNewRecord = !exists)
        aggregateTemplate.save(entity)
    }

    override fun findValidLatest(email: Email, type: OtpType): Otp? {
        val entity = otpRepository.findFirstByEmailAndTypeAndIsUsedFalseOrderByCreatedAtDesc(email.value, type.value)
        return entity?.let {
            val domain = mapper.toDomain(it)
            if (domain.isValid()) domain else null
        }
    }

    override fun markAsUsed(otp: Otp) {
        val entity = otpRepository.findById(otp.id).orElse(null) ?: return
        aggregateTemplate.save(entity.copy(isUsed = true, usedAt = Instant.now()))
    }
}
