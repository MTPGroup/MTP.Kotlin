package tech.hanasaki.azusa.modules.chat.adapter.out.persistence.repository

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.koin.core.context.GlobalContext
import org.koin.dsl.module
import tech.hanasaki.azusa.BaseIntegrationTest
import tech.hanasaki.azusa.modules.auth.domain.port.UserRepositoryPort
import tech.hanasaki.azusa.modules.character.characterModule
import tech.hanasaki.azusa.modules.character.domain.model.Character
import tech.hanasaki.azusa.modules.character.domain.port.CharacterRepositoryPort
import tech.hanasaki.azusa.modules.chat.adapter.`in`.web.chatRoutes
import tech.hanasaki.azusa.modules.chat.chatModule
import tech.hanasaki.azusa.modules.chat.domain.model.*
import tech.hanasaki.azusa.modules.chat.domain.port.ChatRepositoryPort
import tech.hanasaki.azusa.modules.chat.domain.port.MessageRepositoryPort
import tech.hanasaki.azusa.shared.domain.model.vo.Email
import tech.hanasaki.azusa.shared.port.out.TransactionalPort
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * ExposedMessageRepository 集成测试
 */
@ExperimentalUuidApi
class ExposedMessageRepositoryTest : BaseIntegrationTest() {

    override fun additionalModules(config: io.ktor.server.config.ApplicationConfig) = listOf(
        characterModule(),
        chatModule(config)
    )

    override fun io.ktor.server.routing.Route.additionalRoutes() {
        chatRoutes()
    }

    @Test
    fun `findById should return message when exists`() = integrationTest {
        val koin = GlobalContext.get()
        val messageRepository = koin.get<MessageRepositoryPort>()
        val chatRepository = koin.get<ChatRepositoryPort>()
        val userRepository = koin.get<UserRepositoryPort>()
        val characterRepository = koin.get<CharacterRepositoryPort>()
        val tx = koin.get<TransactionalPort>()
        
        // 获取测试用户
        val user = tx.execute {
            userRepository.findByEmail(Email("test-user@example.com"))
        } ?: throw IllegalStateException("Test user not found")
        
        // 创建角色
        val character = Character.create(
            authorId = user.id,
            name = "Test Character"
        )
        tx.execute {
            characterRepository.save(character)
        }
        
        // 创建会话
        val chat = Chat.createPrivateChat(user.id, character.id, "Test Chat")
        tx.execute {
            chatRepository.save(chat)
        }
        
        // 创建消息（使用测试用户作为发送者）
        val message = Message.createText(
            chatId = chat.id,
            senderType = SenderType.USER,
            senderId = user.id.value,
            text = "Test message content"
        )

        tx.execute {
            messageRepository.save(message)
        }

        val foundMessage = tx.execute {
            messageRepository.findById(message.id)
        }

        assertNotNull(foundMessage)
        assertEquals(message.id, foundMessage?.id)
        assertEquals(message.chatId, foundMessage?.chatId)
        assertEquals(message.senderId, foundMessage?.senderId)
        assertEquals("Test message content", foundMessage?.getPlainText())
    }

    @Test
    fun `findById should return null when message does not exist`() = integrationTest {
        val koin = GlobalContext.get()
        val messageRepository = koin.get<MessageRepositoryPort>()
        val tx = koin.get<TransactionalPort>()
        val nonExistentId = MessageId(Uuid.random())

        val foundMessage = tx.execute {
            messageRepository.findById(nonExistentId)
        }

        assertNull(foundMessage)
    }

    @Test
    fun `findById should return correct message with multimodal content`() = integrationTest {
        val koin = GlobalContext.get()
        val messageRepository = koin.get<MessageRepositoryPort>()
        val chatRepository = koin.get<ChatRepositoryPort>()
        val userRepository = koin.get<UserRepositoryPort>()
        val characterRepository = koin.get<CharacterRepositoryPort>()
        val tx = koin.get<TransactionalPort>()
        
        // 获取测试用户
        val user = tx.execute {
            userRepository.findByEmail(Email("test-user@example.com"))
        } ?: throw IllegalStateException("Test user not found")
        
        // 创建角色
        val character = Character.create(
            authorId = user.id,
            name = "Test Character"
        )
        tx.execute {
            characterRepository.save(character)
        }
        
        // 创建会话
        val chat = Chat.createPrivateChat(user.id, character.id, "Test Chat")
        tx.execute {
            chatRepository.save(chat)
        }
        
        // 创建多模态内容消息
        val message = Message.create(
            chatId = chat.id,
            senderType = SenderType.USER,
            senderId = user.id.value,
            content = listOf(
                MessageContent.Text("Hello "),
                MessageContent.Text("World!")
            ),
            metadata = JsonObject(mapOf("key" to JsonPrimitive("value")))
        )

        tx.execute {
            messageRepository.save(message)
        }

        val foundMessage = tx.execute {
            messageRepository.findById(message.id)
        }

        assertNotNull(foundMessage)
        assertEquals(2, foundMessage?.content?.size)
        assertEquals("Hello \nWorld!", foundMessage?.getPlainText())
    }
}
