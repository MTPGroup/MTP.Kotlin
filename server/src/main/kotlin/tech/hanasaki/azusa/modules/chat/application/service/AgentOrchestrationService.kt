package tech.hanasaki.azusa.modules.chat.application.service

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.*
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.eventHandler.feature.EventHandler
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.params.LLMParams
import ai.koog.prompt.streaming.StreamFrame
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
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
import java.util.concurrent.ConcurrentHashMap


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
    private val tx: TransactionalPort,
) : AgentUseCasePort {
    private val logger = KotlinLogging.logger {}

    /** 正在处理中的 chatId 集合，防止同一会话并发请求 */
    private val activeChats = ConcurrentHashMap.newKeySet<ChatId>()

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
    ): Flow<AgentStreamEvent> {
        if (!activeChats.add(chatId)) {
            logger.warn { "会话 $chatId 正在处理中，拒绝重复请求" }
            return flowOf(AgentStreamEvent.Error("该会话正在处理中，请等待完成"))
        }

        return channelFlow {
            val requestId = java.util.UUID.randomUUID().toString().take(8)
            logger.info { "[$requestId] 开始处理消息请求: userId=$userId, chatId=$chatId, messageCount=${userMessage.size}" }

            try {
                // 加载 chat
                logger.debug { "[$requestId] [1/12] 加载会话信息..." }
                val chat = tx.readOnly {
                    chatRepository.findById(chatId) ?: throw NotFoundException("会话不存在")
                }
                logger.debug { "[$requestId] [1/12] 会话加载成功: chatId=$chatId, ownerId=${chat.ownerId}, characterId=${chat.getCharacter()}" }

                if (chat.ownerId != userId) {
                    logger.warn { "[$requestId] 权限拒绝: userId=$userId 尝试访问 chatId=$chatId (owner=${chat.ownerId})" }
                    throw AuthorizationException("您没有权限访问此会话")
                }

                // 加载角色
                logger.debug { "[$requestId] [2/12] 加载角色信息..." }
                val characterId = chat.getCharacter()
                    ?: throw ValidationException("会话中没有角色")
                val character = tx.readOnly {
                    characterRepository.findById(characterId)
                        ?: throw NotFoundException("角色不存在")
                }
                val originPrompt = character.originPrompt ?: ""
                logger.debug { "[$requestId] [2/12] 角色加载成功: characterId=$characterId, name=${character.name}, hasOriginPrompt=${originPrompt.isNotBlank()}" }

                // 加载配置
                logger.debug { "[$requestId] [3/12] 加载聊天配置..." }
                val chatConfig = tx.readOnly {
                    chatConfigRepository.findByChatId(chatId)
                }
                logger.debug { "[$requestId] [3/12] 配置加载完成: hasCustomConfig=${chatConfig != null}" }

                // 加载知识库
                logger.debug { "[$requestId] [4/12] 加载知识库订阅..." }
                val knowledgeSubscriptions = tx.readOnly {
                    knowledgeSubscriptionRepository.findByCharacterId(characterId)
                }
                val knowledgeBaseIds = knowledgeSubscriptions.map { it.knowledgeBaseId.value.toString() }
                logger.debug { "[$requestId] [4/12] 知识库加载完成: count=${knowledgeBaseIds.size}, ids=$knowledgeBaseIds" }

                // 加载插件
                logger.debug { "[$requestId] [5/12] 加载已启用插件..." }
                val enabledPlugins = tx.readOnly {
                    chatPluginSubscriptionRepository.findEnabledByChatId(chatId)
                }
                logger.debug { "[$requestId] [5/12] 插件加载完成: count=${enabledPlugins.size}" }

                // 构建 ToolRegistry
                logger.debug { "[$requestId] [6/12] 构建工具注册表..." }
                val pluginTools = tx.readOnly {
                    enabledPlugins.mapNotNull { sub ->
                        val plugin = pluginRepository.findById(sub.pluginId) ?: return@mapNotNull null
                        pluginToolFactory.create(plugin, sub.config)
                    }
                }
                logger.debug { "[$requestId] [6/12] 插件工具创建完成: count=${pluginTools.size}" }

                val toolRegistry = ToolRegistry {
                    for (pluginTool in pluginTools) {
                        tool(pluginTool)
                        logger.debug { "[$requestId] [6/12] 注册插件工具: ${pluginTool.name}" }
                    }
                    if (knowledgeBaseIds.isNotEmpty()) {
                        val searchTool = SearchKnowledgeTool(knowledgeSearcher, userId, knowledgeBaseIds)
                        tool(searchTool)
                        logger.debug { "[$requestId] [6/12] 注册知识库搜索工具: knowledgeBaseIds=$knowledgeBaseIds" }
                    }
                }
                logger.info { "[$requestId] [6/12] ToolRegistry构建完成: totalTools=${pluginTools.size + if (knowledgeBaseIds.isNotEmpty()) 1 else 0}" }

                // 构建系统提示词和历史
                logger.debug { "[$requestId] [7/12] 构建系统提示词和历史..." }
                val systemPrompt = buildSystemPrompt(originPrompt, chatConfig?.systemPrompt)
                val history = tx.readOnly {
                    messageRepository.findByChatId(chatId)
                }
                val recentHistory = trimHistoryByTokenBudget(history, MAX_HISTORY_TOKENS)
                logger.debug { "[$requestId] [7/12] 历史记录处理完成: totalMessages=${history.size}, recentMessages=${recentHistory.size}, maxTokens=$MAX_HISTORY_TOKENS" }

                // 获取 LLM 配置
                logger.debug { "[$requestId] [8/12] 获取LLM配置..." }
                val llmConfig = tx.readOnly {
                    llmConfigProvider.getActiveConfig(userId)
                } ?: DEFAULT_LLM_CONFIG
                val effectiveConfig = applyOverrides(llmConfig, chatConfig)
                logger.info { "[$requestId] [8/12] LLM配置: provider=${effectiveConfig.provider}, model=${effectiveConfig.model}, temperature=${effectiveConfig.temperature}" }

                // 保存用户消息
                logger.debug { "[$requestId] [9/12] 保存用户消息到数据库..." }
                tx.execute {
                    val userMsg = Message.create(
                        chatId = chatId,
                        senderType = SenderType.USER,
                        senderId = userId.value,
                        content = userMessage,
                    )
                    messageRepository.save(userMsg)
                }
                logger.debug { "[$requestId] [9/12] 用户消息保存成功" }

                // 构建 Prompt
                logger.debug { "[$requestId] [10/12] 构建Agent Prompt..." }
                val userText = userMessage.filterIsInstance<MessageContent.Text>()
                    .joinToString("\n") { it.content }
                logger.debug { "[$requestId] [10/12] 用户输入文本长度: ${userText.length} chars" }

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
                logger.debug { "[$requestId] [10/12] Prompt构建完成: systemPromptLength=${systemPrompt.length}, historyMessages=${recentHistory.size}" }

                // 配置Agent
                logger.debug { "[$requestId] [11/12] 配置AIAgent..." }
                val agentConfig = AIAgentConfig(
                    prompt = agentPrompt,
                    model = effectiveConfig.toLLModel(),
                    maxAgentIterations = MAX_TOOL_CALL_ROUNDS,
                )
                logger.debug { "[$requestId] [11/12] Agent配置完成: maxIterations=$MAX_TOOL_CALL_ROUNDS" }

                // 定义策略图
                val agentStrategy = strategy("chat-agent") {
                    val nodeSendInput by nodeLLMRequest()
                    val nodeExecuteTool by nodeExecuteTool()
                    val nodeSendToolResult by nodeLLMSendToolResult()

                    // Start → 发送用户输入到 LLM
                    edge(nodeStart forwardTo nodeSendInput)

                    // LLM 返回文本 → 结束
                    edge(nodeSendInput forwardTo nodeFinish onAssistantMessage { true })

                    // LLM 调用工具 → 执行工具
                    edge(nodeSendInput forwardTo nodeExecuteTool onToolCall { true })

                    // 工具执行完 → 将结果发回 LLM
                    edge(nodeExecuteTool forwardTo nodeSendToolResult)

                    // LLM 继续调用工具 → 再次执行（多轮循环）
                    edge(nodeSendToolResult forwardTo nodeExecuteTool onToolCall { true })

                    // LLM 返回文本 → 结束
                    edge(nodeSendToolResult forwardTo nodeFinish onAssistantMessage { true })
                }
                logger.debug { "[$requestId] [11/12] 策略图定义完成" }

                // 创建 AIAgent + EventHandler
                logger.info { "[$requestId] [12/12] 启动AIAgent执行流程..." }
                var toolCallCount = 0
                val agent = AIAgent(
                    promptExecutor = promptExecutor,
                    toolRegistry = toolRegistry,
                    strategy = agentStrategy,
                    agentConfig = agentConfig,
                ) {
                    install(EventHandler) {
                        onAgentStarting {
                            logger.info { "[$requestId] Agent启动: strategy=chat-agent" }
                        }
                        onLLMStreamingFrameReceived { ctx ->
                            when (val frame = ctx.streamFrame) {
                                is StreamFrame.Append -> {
                                    logger.debug { "[$requestId] LLM流式输出: deltaLength=${frame.text.length}" }
                                    send(AgentStreamEvent.Delta(frame.text))
                                }

                                is StreamFrame.ToolCall -> {
                                    logger.debug { "[$requestId] LLM工具调用信号: toolCallId=${frame.id}" }
                                }

                                is StreamFrame.End -> {
                                    logger.debug { "[$requestId] LLM流式输出结束" }
                                }
                            }
                        }
                        onToolCallStarting { ctx ->
                            toolCallCount++
                            logger.info { "[$requestId] 工具调用开始 [#$toolCallCount]: tool=${ctx.toolName}, args=${ctx.toolArgs}" }
                            send(AgentStreamEvent.ToolCallStart(ctx.toolName, ctx.toolArgs))
                        }
                        onToolCallCompleted { ctx ->
                            val resultText = ctx.toolResult?.toString() ?: ""
                            val resultPreview =
                                if (resultText.length > 100) resultText.take(100) + "..." else resultText
                            logger.info { "[$requestId] 工具调用完成 [#$toolCallCount]: tool=${ctx.toolName}, resultLength=${resultText.length}, resultPreview=$resultPreview" }
                            send(AgentStreamEvent.ToolCallResult(ctx.toolName, resultText))
                        }
                        onAgentCompleted { ctx ->
                            logger.info { "[$requestId] Agent执行完成: totalToolCalls=$toolCallCount, resultLength=${ctx.result.toString().length}" }
                        }
                    }
                }

                // 运行 agent
                logger.info { "[$requestId] 开始执行Agent.run()..." }
                val startTime = System.currentTimeMillis()
                val result = withContext(Dispatchers.IO) {
                    agent.run(userText)
                }
                val executionTime = System.currentTimeMillis() - startTime
                logger.info { "[$requestId] Agent执行完成: executionTime=${executionTime}ms, resultLength=${result.length}" }

                // 保存助手消息并更新会话
                logger.debug { "[$requestId] 保存助手消息到数据库..." }
                tx.execute {
                    val assistantMsg = Message.createText(
                        chatId = chatId,
                        senderType = SenderType.CHARACTER,
                        senderId = characterId.value,
                        text = result,
                    )
                    messageRepository.save(assistantMsg)
                    chat.updateLastMessage(result.take(100))
                    chatRepository.save(chat)
                }
                logger.debug { "[$requestId] 助手消息保存成功" }

                logger.info { "[$requestId] 请求处理完成: chatId=$chatId, totalTime=${executionTime}ms, toolCalls=$toolCallCount" }
                send(AgentStreamEvent.Done(result))

            } catch (e: Exception) {
                logger.error(e) { "[$requestId] 处理会话消息失败: chatId=$chatId, error=${e.javaClass.simpleName}, message=${e.message}" }
                send(AgentStreamEvent.Error(e.message ?: "未知错误"))
            } finally {
                activeChats.remove(chatId)
            }
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
        base: LLMConfig,
        chatConfig: ChatConfig?,
    ): LLMConfig {
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
