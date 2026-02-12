package tech.hanasaki.azusa.modules.chat.adapter.`in`.event

import io.github.oshai.kotlinlogging.KotlinLogging
import tech.hanasaki.azusa.modules.chat.domain.events.ChatCreated
import tech.hanasaki.azusa.modules.chat.domain.model.ChatConfig
import tech.hanasaki.azusa.modules.chat.domain.port.ChatConfigRepositoryPort
import tech.hanasaki.azusa.shared.port.`in`.DomainEventHandlerPort

private val logger = KotlinLogging.logger {}

class ChatCreatedHandler(
    private val chatConfigRepository: ChatConfigRepositoryPort,
) : DomainEventHandlerPort<ChatCreated> {
    override suspend fun invoke(event: ChatCreated) {
        val config = ChatConfig.create(
            chatId = event.chatId,
        )
        chatConfigRepository.save(config)
    }
}
