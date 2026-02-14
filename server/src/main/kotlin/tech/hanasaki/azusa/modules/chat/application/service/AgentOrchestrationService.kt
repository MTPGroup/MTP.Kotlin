package tech.hanasaki.azusa.modules.chat.application.service

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.TokenStream
import dev.langchain4j.service.tool.ToolExecution
import dev.langchain4j.service.tool.ToolProviderResult
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import tech.hanasaki.azusa.modules.chat.adapter.out.llm.ChatModelFactory
import tech.hanasaki.azusa.modules.chat.application.port.`in`.AgentStreamEvent
import tech.hanasaki.azusa.modules.chat.application.port.`in`.AgentUseCasePort
import tech.hanasaki.azusa.modules.chat.application.port.out.AgentContextLoaderPort
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import tech.hanasaki.azusa.modules.chat.domain.model.Message
import tech.hanasaki.azusa.modules.chat.domain.model.MessageContent
import tech.hanasaki.azusa.modules.chat.domain.model.SenderType
import tech.hanasaki.azusa.modules.chat.domain.port.ChatRepositoryPort
import tech.hanasaki.azusa.modules.chat.domain.port.MessageRepositoryPort
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.shared.port.out.TransactionalPort
import kotlin.coroutines.resumeWithException
import kotlin.time.Clock
import kotlin.uuid.Uuid


class AgentOrchestrationService(
    private val contextLoader: AgentContextLoaderPort,
    private val chatRepository: ChatRepositoryPort,
    private val messageRepository: MessageRepositoryPort,
    private val chatModelFactory: ChatModelFactory,
    private val tx: TransactionalPort,
) : AgentUseCasePort {
    private val logger = KotlinLogging.logger { }

    private val chatMutex = Mutex()
    private val activeChatIds = mutableSetOf<ChatId>()

    private val json = Json { ignoreUnknownKeys = true }

    private interface ChatAssistant {
        fun chat(message: String): TokenStream
    }

    override suspend fun processMessage(
        userId: UserId,
        chatId: ChatId,
        userMessage: List<MessageContent>,
    ): Flow<AgentStreamEvent> {
        val isAccepted = chatMutex.withLock {
            !activeChatIds.contains(chatId)
        }

        if (!isAccepted) {
            logger.warn { "会话 $chatId 正在处理中，拒绝重复请求" }
            return flowOf(AgentStreamEvent.Error("该会话正在处理中，请等待完成"))
        }

        return channelFlow {
            val requestId = Uuid.random().toString().take(8)
            logger.info { "[$requestId] 开始处理消息请求: userId=$userId, chatId=$chatId, messageCount=${userMessage.size}" }

            try {
                // 加载上下文
                val ctx = contextLoader.load(userId, chatId, requestId)

                // 保存用户消息
                tx.execute {
                    val userMsg = Message.create(
                        chatId = chatId,
                        senderType = SenderType.USER,
                        senderId = userId.value,
                        content = userMessage,
                    )
                    messageRepository.save(userMsg)
                }

                // 构建用户消息
                val userText = userMessage.filterIsInstance<MessageContent.Text>()
                    .joinToString("\n") { it.content }

                // 创建流式模型
                val streamingModel = chatModelFactory.create(ctx.effectiveLLMConfig)
                logger.info { "${ctx.effectiveLLMConfig}" }

                // 构建聊天记录并预加载系统提示词
                val chatMemory = MessageWindowChatMemory.builder()
                    .maxMessages(100)
                    .build()

                val systemPrompt = buildSystemPrompt(ctx.originPrompt, ctx.chat.config?.systemPrompt)
                if (systemPrompt.isNotBlank()) {
                    chatMemory.add(SystemMessage.from(systemPrompt))
                }
                for (msg in ctx.recentHistory) {
                    when (msg.senderType) {
                        SenderType.USER -> chatMemory.add(UserMessage.from(msg.getPlainText()))
                        SenderType.CHARACTER -> chatMemory.add(AiMessage.from(msg.getPlainText()))
                    }
                }

                val staticTools = ctx.toolObjects.size
                val dynamicTools = ctx.pluginTools.size
                logger.info {
                    "[$requestId] AI服务构建中... " +
                            "静态工具=$staticTools, " +
                            "动态工具=$dynamicTools, " +
                            "总工具数=${staticTools + dynamicTools}"
                }

                // 构建ai服务
                val builder = AiServices.builder(ChatAssistant::class.java)
                    .streamingChatModel(streamingModel)
                    .chatMemory(chatMemory)

                // 注册 @Tool 注解对象
                if (ctx.toolObjects.isNotEmpty()) {
                    builder.tools(ctx.toolObjects)
                }

                // 通过 toolProvider 注册动态插件工具
                if (ctx.pluginTools.isNotEmpty()) {
                    val pluginToolsCopy = ctx.pluginTools.toMap()
                    builder.toolProvider { _ ->
                        ToolProviderResult.builder()
                            .apply {
                                for ((spec, executor) in pluginToolsCopy) {
                                    add(spec, executor)
                                }
                            }
                            .build()
                    }
                }

                val assistant = builder.build()

                // 执行对话
                logger.info { "[$requestId] 开始执行LLM对话..." }
                val startTime = Clock.System.now().toEpochMilliseconds()
                val fullContent = StringBuilder()
                var toolCallCount = 0

                val tokenStream = assistant.chat(userText)

                val response = suspendCancellableCoroutine<ChatResponse> { continuation ->
                    tokenStream
                        .onPartialResponse { token ->
                            fullContent.append(token)
                            trySend(AgentStreamEvent.Delta(token))
                        }
                        .beforeToolExecution { beforeExec ->
                            toolCallCount++
                            val toolName = beforeExec.request().name()
                            val toolArgs = try {
                                json.parseToJsonElement(beforeExec.request().arguments()) as? JsonObject
                                    ?: JsonObject(emptyMap())
                            } catch (_: Exception) {
                                JsonObject(emptyMap())
                            }
                            logger.info { "[$requestId] 工具调用开始 [#$toolCallCount]: tool=$toolName, args=$toolArgs" }
                            trySend(AgentStreamEvent.ToolCallStart(toolName, toolArgs))
                        }
                        .onToolExecuted { toolExecution: ToolExecution ->
                            val toolName = toolExecution.request().name()
                            val resultText = toolExecution.result()
                            logger.debug { "[$requestId] 工具调用完成 [#$toolCallCount]: tool=$toolName, resultLength=${resultText.length}" }
                            trySend(AgentStreamEvent.ToolCallResult(toolName, resultText))
                        }
                        .onCompleteResponse { response ->
                            continuation.resume(response) { cause, _, _ ->
                                logger.info { "取消对话任务: $cause" }
                            }
                        }
                        .onError { error ->
                            continuation.resumeWithException(error)
                        }
                        .start()
                }

                // 等待结束
                val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
                val result = fullContent.toString().ifEmpty {
                    response.aiMessage().text() ?: ""
                }
                logger.info { "[$requestId] LLM对话完成: executionTime=${executionTime}ms, resultLength=${result.length}" }

                // 保存ai消息
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

                logger.info { "[$requestId] 请求处理完成: chatId=$chatId, totalTime=${executionTime}ms, toolCalls=$toolCallCount" }
                send(AgentStreamEvent.Done(result))

            } catch (e: Exception) {
                logger.error(e) { "[$requestId] 处理会话消息失败: chatId=$chatId, error=${e.javaClass.simpleName}, message=${e.message}" }
                send(AgentStreamEvent.Error(e.message ?: "未知错误"))
            } finally {
                chatMutex.withLock {
                    activeChatIds.remove(chatId)
                }
            }
        }
    }
}
