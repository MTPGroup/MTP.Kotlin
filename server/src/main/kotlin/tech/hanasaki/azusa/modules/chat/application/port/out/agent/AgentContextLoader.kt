package tech.hanasaki.azusa.modules.chat.application.port.out.agent

import ai.koog.agents.core.tools.ToolRegistry
import io.github.oshai.kotlinlogging.KotlinLogging
import tech.hanasaki.azusa.modules.character.domain.port.CharacterRepositoryPort
import tech.hanasaki.azusa.modules.character.domain.port.KnowledgeSubscriptionRepositoryPort
import tech.hanasaki.azusa.modules.chat.adapter.out.knowledge.SearchKnowledgeTool
import tech.hanasaki.azusa.modules.chat.application.port.out.knowledge.KnowledgeSearcher
import tech.hanasaki.azusa.modules.chat.application.port.out.plugin.PluginToolFactory
import tech.hanasaki.azusa.modules.chat.application.service.applyLLMOverrides
import tech.hanasaki.azusa.modules.chat.application.service.trimHistoryByTokenBudget
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import tech.hanasaki.azusa.modules.chat.domain.port.ChatConfigRepositoryPort
import tech.hanasaki.azusa.modules.chat.domain.port.ChatPluginSubscriptionRepositoryPort
import tech.hanasaki.azusa.modules.chat.domain.port.ChatRepositoryPort
import tech.hanasaki.azusa.modules.chat.domain.port.MessageRepositoryPort
import tech.hanasaki.azusa.modules.plugin.domain.port.PluginRepositoryPort
import tech.hanasaki.azusa.shared.domain.exception.AuthorizationException
import tech.hanasaki.azusa.shared.domain.exception.NotFoundException
import tech.hanasaki.azusa.shared.domain.exception.ValidationException
import tech.hanasaki.azusa.shared.domain.model.vo.LLMConfig
import tech.hanasaki.azusa.shared.domain.model.vo.LLMProvider
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.shared.port.out.LLMConfigProvider
import tech.hanasaki.azusa.shared.port.out.TransactionalPort

class AgentContextLoader(
    private val chatRepository: ChatRepositoryPort,
    private val messageRepository: MessageRepositoryPort,
    private val chatConfigRepository: ChatConfigRepositoryPort,
    private val chatPluginSubscriptionRepository: ChatPluginSubscriptionRepositoryPort,
    private val characterRepository: CharacterRepositoryPort,
    private val knowledgeSubscriptionRepository: KnowledgeSubscriptionRepositoryPort,
    private val pluginRepository: PluginRepositoryPort,
    private val knowledgeSearcher: KnowledgeSearcher,
    private val pluginToolFactory: PluginToolFactory,
    private val llmConfigProvider: LLMConfigProvider,
    private val tx: TransactionalPort,
) {
    private val logger = KotlinLogging.logger {}

    companion object {
        private const val MAX_HISTORY_TOKENS = 4000

        private val DEFAULT_LLM_CONFIG = LLMConfig(
            provider = LLMProvider.DEEPSEEK,
            baseUrl = "http://localhost:11434/v1",
            apiKey = "sk-ef5a723504f147efaadaa78c0828ee73",
            model = "deepseek-chat",
            temperature = 0.7f,
        )
    }

    suspend fun load(userId: UserId, chatId: ChatId, requestId: String): AgentContext {
        // 加载 chat
//        logger.debug { "[$requestId] [1/8] 加载会话信息..." }
        val chat = tx.readOnly {
            chatRepository.findById(chatId) ?: throw NotFoundException("会话不存在")
        }
//        logger.debug { "[$requestId] [1/8] 会话加载成功: chatId=$chatId, ownerId=${chat.ownerId}, characterId=${chat.getCharacter()}" }

        if (chat.ownerId != userId) {
            logger.warn { "[$requestId] 权限拒绝: userId=$userId 尝试访问 chatId=$chatId (owner=${chat.ownerId})" }
            throw AuthorizationException("您没有权限访问此会话")
        }

        // 加载角色
//        logger.debug { "[$requestId] [2/8] 加载角色信息..." }
        val characterId = chat.getCharacter()
            ?: throw ValidationException("会话中没有角色")
        val character = tx.readOnly {
            characterRepository.findById(characterId)
                ?: throw NotFoundException("角色不存在")
        }
        val originPrompt = character.originPrompt ?: ""
//        logger.debug { "[$requestId] [2/8] 角色加载成功: characterId=$characterId, name=${character.name}, hasOriginPrompt=${originPrompt.isNotBlank()}" }

        // 加载配置
//        logger.debug { "[$requestId] [3/8] 加载聊天配置..." }
        val chatConfig = tx.readOnly {
            chatConfigRepository.findByChatId(chatId)
        }
//        logger.debug { "[$requestId] [3/8] 配置加载完成: hasCustomConfig=${chatConfig != null}" }

        // 加载知识库
//        logger.debug { "[$requestId] [4/8] 加载知识库订阅..." }
        val knowledgeSubscriptions = tx.readOnly {
            knowledgeSubscriptionRepository.findByCharacterId(characterId)
        }
        val knowledgeBaseIds = knowledgeSubscriptions.map { it.knowledgeBaseId.value.toString() }
//        logger.debug { "[$requestId] [4/8] 知识库加载完成: count=${knowledgeBaseIds.size}, ids=$knowledgeBaseIds" }

        // 加载插件
//        logger.debug { "[$requestId] [5/8] 加载已启用插件..." }
        val enabledPlugins = tx.readOnly {
            chatPluginSubscriptionRepository.findEnabledByChatId(chatId)
        }
//        logger.debug { "[$requestId] [5/8] 插件加载完成: count=${enabledPlugins.size}" }

        // 构建 ToolRegistry
//        logger.debug { "[$requestId] [6/8] 构建工具注册表..." }
        val pluginTools = tx.readOnly {
            enabledPlugins.mapNotNull { sub ->
                val plugin = pluginRepository.findById(sub.pluginId) ?: return@mapNotNull null
                pluginToolFactory.create(plugin, sub.config)
            }
        }
//        logger.debug { "[$requestId] [6/8] 插件工具创建完成: count=${pluginTools.size}" }

        val toolRegistry = ToolRegistry.Companion {
            for (pluginTool in pluginTools) {
                tool(pluginTool)
//                logger.debug { "[$requestId] [6/8] 注册插件工具: ${pluginTool.name}" }
            }
            if (knowledgeBaseIds.isNotEmpty()) {
                val searchTool = SearchKnowledgeTool(knowledgeSearcher, userId, knowledgeBaseIds)
                tool(searchTool)
//                logger.debug { "[$requestId] [6/8] 注册知识库搜索工具: knowledgeBaseIds=$knowledgeBaseIds" }
            }
        }
//        logger.info { "[$requestId] [6/8] ToolRegistry构建完成: totalTools=${pluginTools.size + if (knowledgeBaseIds.isNotEmpty()) 1 else 0}" }

        // 构建系统提示词和历史
//        logger.debug { "[$requestId] [7/8] 构建系统提示词和历史..." }
        val history = tx.readOnly {
            messageRepository.findByChatId(chatId)
        }
        val recentHistory = trimHistoryByTokenBudget(history, MAX_HISTORY_TOKENS)
//        logger.debug { "[$requestId] [7/8] 历史记录处理完成: totalMessages=${history.size}, recentMessages=${recentHistory.size}, maxTokens=$MAX_HISTORY_TOKENS" }

        // 获取 LLM 配置
//        logger.debug { "[$requestId] [8/8] 获取LLM配置..." }
        val llmConfig = tx.readOnly {
            llmConfigProvider.getActiveConfig(userId)
        } ?: DEFAULT_LLM_CONFIG
        val effectiveConfig = applyLLMOverrides(llmConfig, chatConfig)
//        logger.info { "[$requestId] [8/8] LLM配置: provider=${effectiveConfig.provider}, model=${effectiveConfig.model}, temperature=${effectiveConfig.temperature}" }

        return AgentContext(
            chat = chat,
            originPrompt = originPrompt,
            chatConfig = chatConfig,
            knowledgeBaseIds = knowledgeBaseIds,
            toolRegistry = toolRegistry,
            recentHistory = recentHistory,
            effectiveLLMConfig = effectiveConfig,
        )
    }
}