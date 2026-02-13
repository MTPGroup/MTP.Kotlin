package tech.hanasaki.azusa.modules.chat.application.service

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.*
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
import tech.hanasaki.azusa.modules.chat.adapter.out.llm.toLLModel
import tech.hanasaki.azusa.modules.chat.application.port.`in`.AgentStreamEvent
import tech.hanasaki.azusa.modules.chat.application.port.`in`.AgentUseCasePort
import tech.hanasaki.azusa.modules.chat.application.port.out.agent.AgentContextLoader
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import tech.hanasaki.azusa.modules.chat.domain.model.Message
import tech.hanasaki.azusa.modules.chat.domain.model.MessageContent
import tech.hanasaki.azusa.modules.chat.domain.model.SenderType
import tech.hanasaki.azusa.modules.chat.domain.port.ChatRepositoryPort
import tech.hanasaki.azusa.modules.chat.domain.port.MessageRepositoryPort
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.shared.port.out.TransactionalPort
import java.util.concurrent.ConcurrentHashMap


class AgentOrchestrationService(
    private val contextLoader: AgentContextLoader,
    private val chatRepository: ChatRepositoryPort,
    private val messageRepository: MessageRepositoryPort,
    private val promptExecutor: PromptExecutor,
    private val tx: TransactionalPort,
) : AgentUseCasePort {
    private val logger = KotlinLogging.logger {}

    /** 正在处理中的 chatId 集合，防止同一会话并发请求 */
    private val activeChats = ConcurrentHashMap.newKeySet<ChatId>()

    companion object {
        private const val MAX_TOOL_CALL_ROUNDS = 5
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
                // 加载上下文（chat、角色、配置、知识库、插件、工具、历史、LLM配置）
                val ctx = contextLoader.load(userId, chatId, requestId)

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

                val systemPrompt = buildSystemPrompt(ctx.originPrompt, ctx.chatConfig?.systemPrompt)
                val agentPrompt = prompt(
                    id = "agent-chat",
                    params = LLMParams(temperature = ctx.effectiveLLMConfig.temperature.toDouble()),
                ) {
                    system(systemPrompt)
                    for (msg in ctx.recentHistory) {
                        when (msg.senderType) {
                            SenderType.USER -> user(msg.getPlainText())
                            SenderType.CHARACTER -> assistant(msg.getPlainText())
                        }
                    }
                }
                logger.debug { "[$requestId] [10/12] Prompt构建完成: systemPromptLength=${systemPrompt.length}, historyMessages=${ctx.recentHistory.size}" }

                // 配置Agent
                logger.debug { "[$requestId] [11/12] 配置AIAgent..." }
                val agentConfig = AIAgentConfig(
                    prompt = agentPrompt,
                    model = ctx.effectiveLLMConfig.toLLModel(),
                    maxAgentIterations = MAX_TOOL_CALL_ROUNDS,
                )
                logger.debug { "[$requestId] [11/12] Agent配置完成: maxIterations=$MAX_TOOL_CALL_ROUNDS" }

                // 定义策略图
                val agentStrategy = strategy("chat-agent") {
                    val nodeSendInput by nodeLLMRequest()
                    val nodeExecuteTool by nodeExecuteTool()
                    val nodeSendToolResult by nodeLLMSendToolResult()

                    edge(nodeStart forwardTo nodeSendInput)
                    edge(nodeSendInput forwardTo nodeFinish onAssistantMessage { true })
                    edge(nodeSendInput forwardTo nodeExecuteTool onToolCall { true })
                    edge(nodeExecuteTool forwardTo nodeSendToolResult)
                    edge(nodeSendToolResult forwardTo nodeExecuteTool onToolCall { true })
                    edge(nodeSendToolResult forwardTo nodeFinish onAssistantMessage { true })
                }
                logger.debug { "[$requestId] [11/12] 策略图定义完成" }

                // 创建 AIAgent + EventHandler
                logger.info { "[$requestId] [12/12] 启动AIAgent执行流程..." }
                var toolCallCount = 0
                val agent = AIAgent(
                    promptExecutor = promptExecutor,
                    toolRegistry = ctx.toolRegistry,
                    strategy = agentStrategy,
                    agentConfig = agentConfig,
                ) {
                    install(EventHandler) {
                        onAgentStarting {
                            logger.info { "[$requestId] Agent启动: strategy=chat-agent" }
                        }
                        onLLMStreamingFrameReceived { frame ->
                            when (val sf = frame.streamFrame) {
                                is StreamFrame.Append -> {
                                    logger.debug { "[$requestId] LLM流式输出: deltaLength=${sf.text.length}" }
                                    send(AgentStreamEvent.Delta(sf.text))
                                }

                                is StreamFrame.ToolCall -> {
                                    logger.debug { "[$requestId] LLM工具调用信号: toolCallId=${sf.id}" }
                                }

                                is StreamFrame.End -> {
                                    logger.debug { "[$requestId] LLM流式输出结束" }
                                }
                            }
                        }
                        onToolCallStarting { tcCtx ->
                            toolCallCount++
                            logger.info { "[$requestId] 工具调用开始 [#$toolCallCount]: tool=${tcCtx.toolName}, args=${tcCtx.toolArgs}" }
                            send(AgentStreamEvent.ToolCallStart(tcCtx.toolName, tcCtx.toolArgs))
                        }
                        onToolCallCompleted { tcCtx ->
                            val resultText = tcCtx.toolResult?.toString() ?: ""
                            val resultPreview =
                                if (resultText.length > 100) resultText.take(100) + "..." else resultText
                            logger.info { "[$requestId] 工具调用完成 [#$toolCallCount]: tool=${tcCtx.toolName}, resultLength=${resultText.length}, resultPreview=$resultPreview" }
                            send(AgentStreamEvent.ToolCallResult(tcCtx.toolName, resultText))
                        }
                        onAgentCompleted { acCtx ->
                            logger.info { "[$requestId] Agent执行完成: totalToolCalls=$toolCallCount, resultLength=${acCtx.result.toString().length}" }
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
                val characterId = ctx.chat.getCharacter()!!
                tx.execute {
                    val assistantMsg = Message.createText(
                        chatId = chatId,
                        senderType = SenderType.CHARACTER,
                        senderId = characterId.value,
                        text = result,
                    )
                    messageRepository.save(assistantMsg)
                    ctx.chat.updateLastMessage(result.take(100))
                    chatRepository.save(ctx.chat)
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
}
