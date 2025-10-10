package tech.hanasaki.momotalk_plus.features.chats.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.*
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import tech.hanasaki.momotalk_plus.db.AppDatabase
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.mapper.ChatMapper.toChat
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.mapper.ChatMapper.toEntity
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.mapper.MessageMapper.toEntity
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.mapper.MessageMapper.toMessage
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.ChatRemoteDatasource
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.api.ChatApi
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto.CreateChatRequest
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto.UpdateChatInfoRequest
import tech.hanasaki.momotalk_plus.features.chats.domain.model.Chat
import tech.hanasaki.momotalk_plus.features.chats.domain.model.ChatWithCharacter
import tech.hanasaki.momotalk_plus.features.chats.domain.model.Message
import tech.hanasaki.momotalk_plus.features.chats.domain.model.StreamEvent
import tech.hanasaki.momotalk_plus.features.chats.domain.repository.ChatRepository
import kotlin.time.Duration.Companion.minutes

class ChatRepositoryImpl(
    private val chatApi: ChatApi,
    private val remoteDatasource: ChatRemoteDatasource,
    database: AppDatabase,
) : ChatRepository {
    private val chatDao = database.chatDao()
    private val messageDao = database.messageDao()
    private val characterDao = database.characterDao()

    /**
     * 聊天列表 Store
     * 缓存策略: 5分钟后过期
     */
    private val chatListStore: Store<Unit, List<Chat>> = StoreBuilder
        .from(
            fetcher = Fetcher.of { _: Unit ->
                val response = chatApi.getChatList()
                response.data.chats
            },
            sourceOfTruth = SourceOfTruth.of(
                reader = { _: Unit ->
                    chatDao.getAllChats().map { entities ->
                        entities.map { it.toChat() }
                    }
                },
                writer = { _: Unit, chats: List<Chat> ->
                    // 清空旧数据，插入新数据
                    chatDao.deleteAll()
                    chatDao.upsertAll(chats.map { it.toEntity() })
                },
                delete = { _: Unit ->
                    chatDao.deleteAll()
                },
                deleteAll = {
                    chatDao.deleteAll()
                }
            )
        )
        .cachePolicy(
            MemoryPolicy.builder<Unit, List<Chat>>()
                .setExpireAfterWrite(5.minutes)
                .build()
        )
        .build()

    /**
     * 聊天信息 Store (按 chatId 缓存)
     * 缓存策略: 10分钟后过期
     */
    private val chatInfoStore: Store<String, ChatWithCharacter> = StoreBuilder
        .from(
            fetcher = Fetcher.of { chatId: String ->
                val response = chatApi.getChatInfo(chatId)
                response.data
            },
            sourceOfTruth = SourceOfTruth.of(
                reader = { chatId: String ->
                    // 组合 Chat 和 Character 数据构建 ChatWithCharacter
                    combine(
                        chatDao.getChatById(chatId),
                        characterDao.getAllCharacter()
                    ) { chatEntity, characters ->
                        chatEntity?.let { chat ->
                            // 从角色列表中查找对应的角色
                            val character = characters.firstOrNull { it.id == chat.characterId }
                            character?.let {
                                ChatWithCharacter(
                                    id = chat.id,
                                    creatorId = chat.creatorId,
                                    characterId = chat.characterId,
                                    title = chat.title,
                                    description = chat.description,
                                    avatarUrl = chat.avatarUrl,
                                    lastMessage = chat.lastMessage,
                                    createdAt = chat.createdAt,
                                    updatedAt = chat.updatedAt,
                                    character = tech.hanasaki.momotalk_plus.features.chats.domain.model.CharacterSummary(
                                        id = it.id,
                                        name = it.name,
                                        avatarUrl = it.avatarUrl
                                    )
                                )
                            }
                        }
                    }
                },
                writer = { _: String, data: ChatWithCharacter ->
                    // 保存聊天基本信息
                    chatDao.upsert(
                        Chat(
                            id = data.id,
                            creatorId = data.creatorId,
                            characterId = data.characterId,
                            title = data.title,
                            description = data.description,
                            avatarUrl = data.avatarUrl,
                            lastMessage = data.lastMessage,
                            createdAt = data.createdAt,
                            updatedAt = data.updatedAt,
                        ).toEntity()
                    )
                },
                delete = { chatId: String ->
                    chatDao.deleteChat(chatId)
                },
                deleteAll = {
                    chatDao.deleteAll()
                }
            )
        )
        .cachePolicy(
            MemoryPolicy.builder<String, ChatWithCharacter>()
                .setExpireAfterWrite(10.minutes)
                .build()
        )
        .build()

    /**
     * 消息历史 Store (按 chatId 缓存)
     * 缓存策略: 3分钟后过期
     */
    private fun createMessageStore(chatId: String, limit: Int?): Store<Unit, List<Message>> {
        return StoreBuilder
            .from(
                fetcher = Fetcher.of { _: Unit ->
                    val response = chatApi.getChatMessages(chatId, limit)
                    response.data.messages
                },
                sourceOfTruth = SourceOfTruth.of(
                    reader = { _: Unit ->
                        if (limit != null) {
                            messageDao.getMessagesByChatIdWithLimit(chatId, limit)
                        } else {
                            messageDao.getMessagesByChatId(chatId)
                        }.map { entities ->
                            entities.map { it.toMessage() }
                        }
                    },
                    writer = { _: Unit, messages: List<Message> ->
                        // 清空该聊天的旧消息，插入新消息
                        messageDao.deleteMessagesByChatId(chatId)
                        messageDao.upsertAll(messages.map { it.toEntity() })
                    },
                    delete = { _: Unit ->
                        messageDao.deleteMessagesByChatId(chatId)
                    },
                    deleteAll = {
                        messageDao.deleteAll()
                    }
                )
            )
            .cachePolicy(
                MemoryPolicy.builder<Unit, List<Message>>()
                    .setExpireAfterWrite(3.minutes)
                    .build()
            )
            .build()
    }

    override suspend fun createChat(
        characterId: String,
        title: String,
        description: String?,
        avatarUrl: String?,
    ) {
        chatApi.createChat(
            CreateChatRequest(
                characterId,
                title,
                description,
                avatarUrl,
            )
        )
        // 刷新聊天列表
        chatListStore.fresh(Unit)
    }

    override fun getChatList(): Flow<List<Chat>> {
        return chatListStore.stream(
            StoreReadRequest.cached(Unit, refresh = false)
        ).map { response ->
            when (response) {
                is StoreReadResponse.Data -> response.value
                is StoreReadResponse.Loading -> emptyList()
                is StoreReadResponse.Error -> {
                    println("获取聊天列表失败: ${response.errorMessageOrNull()}")
                    emptyList()
                }

                is StoreReadResponse.NoNewData -> emptyList()
                else -> emptyList()
            }
        }
    }

    override suspend fun deleteChat(chatId: String) {
        chatApi.deleteChat(chatId)
        // 删除本地缓存
        chatDao.deleteChat(chatId)
        messageDao.deleteMessagesByChatId(chatId)
        // 刷新聊天列表
        chatListStore.fresh(Unit)
    }

    override suspend fun updateChatInfo(
        chatId: String,
        title: String,
        description: String,
        avatarUrl: String,
    ) {
        chatApi.updateChatInfo(
            chatId,
            UpdateChatInfoRequest(
                title,
                description,
                avatarUrl
            )
        )
        // 刷新聊天列表和聊天信息
        chatListStore.fresh(Unit)
        chatInfoStore.fresh(chatId)
    }

    override fun getChatInfo(chatId: String): Flow<ChatWithCharacter> {
        return chatInfoStore.stream(
            StoreReadRequest.cached(chatId, refresh = false)
        ).map { response ->
            when (response) {
                is StoreReadResponse.Data -> response.value
                else ->
                    throw Exception("获取聊天信息失败: ${response.errorMessageOrNull()}")
            }
        }
    }

    override fun getChatHistory(chatId: String, limits: Int?): Flow<List<Message>> {
        val store = createMessageStore(chatId, limits)
        return store.stream(
            StoreReadRequest.cached(Unit, refresh = false)
        ).map { response ->
            when (response) {
                is StoreReadResponse.Data -> response.value
                is StoreReadResponse.Loading -> emptyList()
                is StoreReadResponse.Error -> {
                    println("获取消息历史失败: ${response.errorMessageOrNull()}")
                    emptyList()
                }

                is StoreReadResponse.NoNewData -> emptyList()
                else -> emptyList()
            }
        }
    }

    override suspend fun clearChatHistory(chatId: String) {
        chatApi.clearChatHistory(chatId)
        // 清空本地消息缓存
        messageDao.deleteMessagesByChatId(chatId)
    }

    override fun sendMessageStream(
        chatId: String,
        message: String,
    ): Flow<StreamEvent> {
        return remoteDatasource.sendMessageStream(chatId, message)
    }
}