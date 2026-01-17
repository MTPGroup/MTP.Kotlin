package tech.hanasaki.azusa.auth.internal.infrastructure.persistence.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.stereotype.Repository
import tech.hanasaki.azusa.auth.internal.domain.model.Email
import tech.hanasaki.azusa.auth.internal.domain.model.User
import tech.hanasaki.azusa.auth.internal.domain.model.UserId
import tech.hanasaki.azusa.auth.internal.domain.repository.UserRepository
import tech.hanasaki.azusa.auth.internal.infrastructure.persistence.mapper.UserEntityMapper
import java.time.Instant

@Repository
class JdbcUserRepository(
    private val aggregateTemplate: JdbcAggregateTemplate,
    private val userEntityRepository: SpringDataUserEntityRepository,
    private val mapper: UserEntityMapper,
) : UserRepository {

    override suspend fun findByEmail(email: Email): User? = withContext(Dispatchers.IO) {
        userEntityRepository.findByEmail(email.value)?.let(mapper::toDomain)
    }

    override suspend fun findById(id: UserId): User? = withContext(Dispatchers.IO) {
        userEntityRepository.findById(id.value).orElse(null)?.let(mapper::toDomain)
    }

    override suspend fun save(user: User) {
        withContext(Dispatchers.IO) {
            val existingUser = userEntityRepository.findById(user.id.value).map { it }.orElse(null)
            val entity = mapper.toEntity(user, Instant.now())
            if (existingUser == null) {
                aggregateTemplate.insert(entity)
            } else {
                aggregateTemplate.save(entity)
            }
        }
    }
}
