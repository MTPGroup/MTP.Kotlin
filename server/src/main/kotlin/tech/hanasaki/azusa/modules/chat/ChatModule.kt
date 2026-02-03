package tech.hanasaki.azusa.modules.chat

import io.ktor.server.config.*
import org.koin.dsl.module
import tech.hanasaki.azusa.modules.chat.domain.repository.ChatConfigRepository
import tech.hanasaki.azusa.modules.chat.domain.repository.ChatMemberRepository
import tech.hanasaki.azusa.modules.chat.domain.repository.ChatPluginSubscriptionRepository
import tech.hanasaki.azusa.modules.chat.domain.repository.ChatRepository
import tech.hanasaki.azusa.modules.chat.domain.repository.MessageRepository
import tech.hanasaki.azusa.modules.chat.infrastructure.persistence.repository.ExposedChatConfigRepository
import tech.hanasaki.azusa.modules.chat.infrastructure.persistence.repository.ExposedChatMemberRepository
import tech.hanasaki.azusa.modules.chat.infrastructure.persistence.repository.ExposedChatPluginSubscriptionRepository
import tech.hanasaki.azusa.modules.chat.infrastructure.persistence.repository.ExposedChatRepository
import tech.hanasaki.azusa.modules.chat.infrastructure.persistence.repository.ExposedMessageRepository

fun chatModule(config: ApplicationConfig) = module {
    single<ChatMemberRepository> { ExposedChatMemberRepository() }
    single<ChatConfigRepository> { ExposedChatConfigRepository() }
    single<ChatPluginSubscriptionRepository> { ExposedChatPluginSubscriptionRepository() }
    single<ChatRepository> { ExposedChatRepository(get()) }
    single<MessageRepository> { ExposedMessageRepository() }
}
