package tech.hanasaki.azusa.modules.chat.adapter.out.agent

import dev.langchain4j.agent.tool.ToolSpecification
import dev.langchain4j.service.tool.ToolExecutor
import io.github.oshai.kotlinlogging.KotlinLogging
import tech.hanasaki.azusa.modules.character.domain.port.CharacterRepositoryPort
import tech.hanasaki.azusa.modules.character.domain.port.KnowledgeSubscriptionRepositoryPort
import tech.hanasaki.azusa.modules.chat.adapter.out.knowledge.SearchKnowledgeTool
import tech.hanasaki.azusa.modules.chat.application.port.out.AgentContext
import tech.hanasaki.azusa.modules.chat.application.port.out.AgentContextLoaderPort
import tech.hanasaki.azusa.modules.chat.application.port.out.KnowledgeSearcherPort
import tech.hanasaki.azusa.modules.chat.application.port.out.PluginToolFactoryPort
import tech.hanasaki.azusa.modules.chat.application.service.applyLLMOverrides
import tech.hanasaki.azusa.modules.chat.application.service.trimHistoryByTokenBudget
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import tech.hanasaki.azusa.modules.chat.domain.port.ChatRepositoryPort
import tech.hanasaki.azusa.modules.chat.domain.port.MessageRepositoryPort
import tech.hanasaki.azusa.modules.plugin.domain.model.PluginStatus
import tech.hanasaki.azusa.modules.plugin.domain.port.PluginRepositoryPort
import tech.hanasaki.azusa.shared.domain.exception.AuthorizationException
import tech.hanasaki.azusa.shared.domain.exception.NotFoundException
import tech.hanasaki.azusa.shared.domain.exception.ValidationException
import tech.hanasaki.azusa.shared.domain.model.vo.LLMConfig
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.shared.port.out.LLMConfigProvider
import tech.hanasaki.azusa.shared.port.out.TransactionalPort

class AgentContextLoader(
    private val chatRepository: ChatRepositoryPort,
    private val messageRepository: MessageRepositoryPort,
    private val characterRepository: CharacterRepositoryPort,
    private val knowledgeSubscriptionRepository: KnowledgeSubscriptionRepositoryPort,
    private val pluginRepository: PluginRepositoryPort,
    private val knowledgeSearcher: KnowledgeSearcherPort,
    private val pluginToolFactory: PluginToolFactoryPort,
    private val llmConfigProvider: LLMConfigProvider,
    private val officialLLMConfig: LLMConfig,
    private val tx: TransactionalPort,
) : AgentContextLoaderPort {
    private val logger = KotlinLogging.logger {}

    companion object {
        private const val MAX_HISTORY_TOKENS = 4000
    }

    override suspend fun load(userId: UserId, chatId: ChatId, requestId: String): AgentContext {
        val chat = tx.readOnly {
            chatRepository.findById(chatId) ?: throw NotFoundException("会话不存在")
        }

        if (chat.ownerId != userId) {
            logger.warn { "[$requestId] 权限拒绝: userId=$userId 尝试访问 chatId=$chatId (owner=${chat.ownerId})" }
            throw AuthorizationException("您没有权限访问此会话")
        }

        val characterId = chat.getCharacter()
            ?: throw ValidationException("会话中没有角色")
        val character = tx.readOnly {
            characterRepository.findById(characterId)
                ?: throw NotFoundException("角色不存在")
        }
        val originPrompt = character.originPrompt ?: ""

        val knowledgeSubscriptions = tx.readOnly {
            knowledgeSubscriptionRepository.findByCharacterId(characterId)
        }
        val knowledgeBaseIds = knowledgeSubscriptions.map { it.knowledgeBaseId.value.toString() }

        val enabledPlugins = chat.pluginSubscriptions.filter { it.enabled }

        val pluginToolMap = mutableMapOf<ToolSpecification, ToolExecutor>()
        tx.readOnly {
            for (sub in enabledPlugins) {
                val plugin = pluginRepository.findById(sub.pluginId) ?: continue
                if (plugin.status != PluginStatus.APPROVED) {
                    logger.warn { "[$requestId] 跳过未通过审核插件: pluginId=${sub.pluginId.value}, status=${plugin.status}" }
                    continue
                }
                val entry = pluginToolFactory.create(plugin, sub.config, userId)
                pluginToolMap[entry.specification] = entry.executor
            }
        }

        val toolObjects = mutableListOf<Any>()
        if (knowledgeBaseIds.isNotEmpty()) {
            toolObjects.add(SearchKnowledgeTool(knowledgeSearcher, userId, knowledgeBaseIds))
        }

        val history = tx.readOnly {
            messageRepository.findByChatId(chatId)
        }
        val recentHistory = trimHistoryByTokenBudget(history, MAX_HISTORY_TOKENS)

        val llmConfig = tx.readOnly {
            llmConfigProvider.getActiveConfig(userId)
        } ?: officialLLMConfig
        val effectiveConfig = applyLLMOverrides(llmConfig, chat.config)

        return AgentContext(
            chat = chat,
            originPrompt = originPrompt,
            knowledgeBaseIds = knowledgeBaseIds,
            pluginTools = pluginToolMap,
            toolObjects = toolObjects,
            recentHistory = recentHistory,
            effectiveLLMConfig = effectiveConfig,
        )
    }
}
