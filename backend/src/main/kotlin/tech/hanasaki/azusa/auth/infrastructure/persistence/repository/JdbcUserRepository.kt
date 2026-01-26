package tech.hanasaki.azusa.auth.infrastructure.persistence.repository

import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.stereotype.Repository
import tech.hanasaki.azusa.auth.domain.model.Email
import tech.hanasaki.azusa.auth.domain.model.User
import tech.hanasaki.azusa.auth.domain.repository.UserRepository
import tech.hanasaki.azusa.auth.infrastructure.persistence.mapper.UserEntityMapper
import tech.hanasaki.azusa.common.UserId
import java.time.Instant

@Repository
class JdbcUserRepository(
    private val aggregateTemplate: JdbcAggregateTemplate,
    private val userEntityRepository: SpringDataUserEntityRepository,
    private val mapper: UserEntityMapper,
) : UserRepository {

    override fun findByEmail(email: Email): User? =
        userEntityRepository.findByEmail(email.value)?.let(mapper::toDomain)

    override fun findById(id: UserId): User? =
        userEntityRepository.findById(id.value).orElse(null)?.let(mapper::toDomain)

    override fun save(user: User) {
        val exists = userEntityRepository.existsById(user.id.value)
        val entity = mapper.toEntity(user, Instant.now(), !exists)
        aggregateTemplate.save(entity)
    }

    override fun deleteById(id: UserId) {
        userEntityRepository.deleteById(id.value)
    }
}
