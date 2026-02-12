package tech.hanasaki.azusa.modules.chat.application.service

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.eventHandler.feature.EventHandler
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.params.LLMParams
import ai.koog.prompt.streaming.StreamFrame
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import tech.hanasaki.azusa.modules.character.domain.port.CharacterRepositoryPort
import tech.hanasaki.azusa.modules.character.domain.port.KnowledgeSubscriptionRepositoryPort
import tech.hanasaki.azusa.modules.chat.adapter.out.knowledge.SearchKnowledgeTool
import tech.hanasaki.azusa.modules.chat.adapter.out.llm.toLLModel
import tech.hanasaki.azusa.modules.chat.application.port.`in`.AgentStreamEvent
import tech.hanasaki.azusa.modules.chat.application.port.`in`.AgentUseCasePort
import tech.hanasaki.azusa.modules.chat.application.port.out.knowledge.KnowledgeSearcher
import tech.hanasaki.azusa.modules.chat.application.port.out.plugin.PluginToolFactory
import tech.hanasaki.azusa.modules.chat.domain.model.*
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

private val logger = KotlinLogging.logger {}

class AgentOrchestrationService(
    private val chatRepository: ChatRepositoryPort,
    private val messageRepository: MessageRepositoryPort,
    private val chatConfigRepository: ChatConfigRepositoryPort,
    private val chatPluginSubscriptionRepository: ChatPluginSubscriptionRepositoryPort,
    private val characterRepository: CharacterRepositoryPort,
    private val knowledgeSubscriptionRepository: KnowledgeSubscriptionRepositoryPort,
    private val pluginRepository: PluginRepositoryPort,
    private val knowledgeSearcher: KnowledgeSearcher,
    private val pluginToolFactory: PluginToolFactory,
    private val promptExecutor: PromptExecutor,
    private val llmConfigProvider: LLMConfigProvider,
    private val transactional: TransactionalPort,
) : AgentUseCasePort {

    companion object {
        private const val MAX_HISTORY_TOKENS = 4000
        private const val MAX_TOOL_CALL_ROUNDS = 5

        private val DEFAULT_LLM_CONFIG = LLMConfig(
            provider = LLMProvider.CUSTOM,
            baseUrl = "http://localhost:11434/v1",
            apiKey = "ollama",
            model = "qwen3:4b",
            temperature = 0.7f,
        )
    }

    override fun processMessage(
        userId: UserId,
        chatId: ChatId,
        userMessage: List<MessageContent>,
    ): Flow<AgentStreamEvent> = channelFlow {
        try {
            logger.debug { "开始处理消息……" }

            // 1. 加载 chat
            val chat = transactional.readOnly {
                chatRepository.findById(chatId) ?: throw NotFoundException("会话不存在")
            }
            if (chat.ownerId != userId) {
                throw AuthorizationException("您没有权限访问此会话")
            }

            // 2. 加载角色
            val characterId = chat.getCharacter()
                ?: throw ValidationException("会话中没有角色")
            val character = transactional.readOnly {
                characterRepository.findById(characterId)
                    ?: throw NotFoundException("角色不存在")
            }
            val originPrompt = character.originPrompt ?: ""

            // 3. 加载配置
            val chatConfig = transactional.readOnly {
                chatConfigRepository.findByChatId(chatId)
            }

            // 4. 加载知识库
            val knowledgeSubscriptions = transactional.readOnly {
                knowledgeSubscriptionRepository.findByCharacterId(characterId)
            }
            val knowledgeBaseIds = knowledgeSubscriptions.map { it.knowledgeBaseId.value.toString() }

            // 5. 加载插件
            val enabledPlugins = transactional.readOnly {
                chatPluginSubscriptionRepository.findEnabledByChatId(chatId)
            }

            // 6. 构建 ToolRegistry（先加载插件，再构建 registry）
            val pluginTools = transactional.readOnly {
                enabledPlugins.mapNotNull { sub ->
                    val plugin = pluginRepository.findById(sub.pluginId) ?: return@mapNotNull null
                    pluginToolFactory.create(plugin, sub.config)
                }
            }

            val toolRegistry = ToolRegistry {
                for (pluginTool in pluginTools) {
                    tool(pluginTool)
                }
                if (knowledgeBaseIds.isNotEmpty()) {
                    tool(SearchKnowledgeTool(knowledgeSearcher, userId, knowledgeBaseIds))
                }
            }

            // 7. 构建系统提示词和历史
            val systemPrompt = buildSystemPrompt(originPrompt, chatConfig?.systemPrompt)
            val history = transactional.readOnly {
                messageRepository.findByChatId(chatId)
            }
            val recentHistory = trimHistoryByTokenBudget(history, MAX_HISTORY_TOKENS)

            // 8. 获取 LLM 配置
            val llmConfig = transactional.readOnly {
                llmConfigProvider.getActiveConfig(userId)
            } ?: DEFAULT_LLM_CONFIG
            val effectiveConfig = applyOverrides(llmConfig, chatConfig)

            // 9. 保存用户消息
            transactional.execute {
                val userMsg = Message.create(
                    chatId = chatId,
                    senderType = SenderType.USER,
                    content = userMessage,
                )
                messageRepository.save(userMsg)
            }

            // 10. 构建 Prompt
            val userText = userMessage.filterIsInstance<MessageContent.Text>()
                .joinToString("\n") { it.content }

            val agentPrompt = prompt(
                id = "agent-chat",
                params = LLMParams(temperature = effectiveConfig.temperature.toDouble()),
            ) {
                system(systemPrompt)
                for (msg in recentHistory) {
                    when (msg.senderType) {
                        SenderType.USER -> user(msg.getPlainText())
                        SenderType.CHARACTER -> assistant(msg.getPlainText())
                    }
                }
            }

            val agentConfig = AIAgentConfig(
                prompt = agentPrompt,
                model = effectiveConfig.toLLModel(),
                maxAgentIterations = MAX_TOOL_CALL_ROUNDS,
            )

            // 11. 创建 AIAgent + EventHandler 桥接到 channelFlow
            val agent = AIAgent(
                promptExecutor = promptExecutor,
                agentConfig = agentConfig,
                toolRegistry = toolRegistry,
            ) {
                install(EventHandler) {
                    onLLMStreamingFrameReceived { ctx ->
                        when (val frame = ctx.streamFrame) {
                            is StreamFrame.Append -> {
                                send(AgentStreamEvent.Delta(frame.text))
                            }

                            else -> { /* ToolCall / End frames handled by agent internally */
                            }
                        }
                    }
                    onToolCallStarting { ctx ->
                        send(AgentStreamEvent.ToolCallStart(ctx.toolName, ctx.toolArgs))
                    }
                    onToolCallCompleted { ctx ->
                        val resultText = ctx.toolResult?.toString() ?: ""
                        send(AgentStreamEvent.ToolCallResult(ctx.toolName, resultText))
                    }
                }
            }

            // 12. 运行 agent
            val result = agent.run(userText)

            // 13. 保存助手消息并更新会话
            transactional.execute {
                val assistantMsg = Message.createText(
                    chatId = chatId,
                    senderType = SenderType.CHARACTER,
                    text = result,
                )
                messageRepository.save(assistantMsg)
                chat.updateLastMessage(result.take(100))
                chatRepository.save(chat)
            }

            send(AgentStreamEvent.Done(result))
        } catch (e: Exception) {
            logger.error(e) { "Error processing message for chat $chatId" }
            send(AgentStreamEvent.Error(e.message ?: "Unknown error"))
        }
    }

    private fun buildSystemPrompt(
        originPrompt: String,
        chatSystemPrompt: String?,
    ): String {
        val parts = mutableListOf<String>()
        if (originPrompt.isNotBlank()) {
            parts.add(originPrompt)
        }
        if (!chatSystemPrompt.isNullOrBlank()) {
            parts.add(chatSystemPrompt)
        }
        return parts.joinToString("\n\n")
    }

    private fun applyOverrides(
        base: tech.hanasaki.azusa.shared.domain.model.vo.LLMConfig,
        chatConfig: ChatConfig?,
    ): tech.hanasaki.azusa.shared.domain.model.vo.LLMConfig {
        if (chatConfig == null) return base
        return base.copy(
            temperature = chatConfig.temperature?.toFloat() ?: base.temperature,
            maxTokens = chatConfig.maxTokens ?: base.maxTokens,
        )
    }

    private fun estimateTokens(text: String): Int {
        var count = 0
        for (char in text) {
            count += if (char.code > 0x4E00) 2 else 1
        }
        return count / 3
    }

    private fun trimHistoryByTokenBudget(messages: List<Message>, budget: Int): List<Message> {
        var remaining = budget
        val result = mutableListOf<Message>()
        for (msg in messages.asReversed()) {
            val tokens = estimateTokens(msg.getPlainText())
            if (remaining - tokens < 0 && result.isNotEmpty()) break
            result.add(msg)
            remaining -= tokens
        }
        return result.asReversed()
    }
}
