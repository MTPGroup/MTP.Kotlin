package tech.hanasaki.azusa.modules.chat.application.service

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.TokenStream
import dev.langchain4j.service.tool.ToolExecution
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOf
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
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid


class AgentOrchestrationService(
    private val contextLoader: AgentContextLoaderPort,
    private val chatRepository: ChatRepositoryPort,
    private val messageRepository: MessageRepositoryPort,
    private val chatModelFactory: ChatModelFactory,
    private val tx: TransactionalPort,
) : AgentUseCasePort {
    private val logger = KotlinLogging.logger { }

    private val activeChats = ConcurrentHashMap.newKeySet<ChatId>()

    private val json = Json { ignoreUnknownKeys = true }

    private interface ChatAssistant {
        fun chat(message: String): TokenStream
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
            val requestId = Uuid.random().toString().take(8)
            logger.info { "[$requestId] 开始处理消息请求: userId=$userId, chatId=$chatId, messageCount=${userMessage.size}" }

            try {
                // Load context
                val ctx = contextLoader.load(userId, chatId, requestId)

                // Save user message
                tx.execute {
                    val userMsg = Message.create(
                        chatId = chatId,
                        senderType = SenderType.USER,
                        senderId = userId.value,
                        content = userMessage,
                    )
                    messageRepository.save(userMsg)
                }

                // Build user text
                val userText = userMessage.filterIsInstance<MessageContent.Text>()
                    .joinToString("\n") { it.content }

                // Create StreamingChatModel
                val streamingModel = chatModelFactory.create(ctx.effectiveLLMConfig)

                // Build ChatMemory and preload system prompt + history
                val chatMemory = MessageWindowChatMemory.builder()
                    .maxMessages(100)
                    .build()

                val systemPrompt = buildSystemPrompt(ctx.originPrompt, ctx.chatConfig?.systemPrompt)
                if (systemPrompt.isNotBlank()) {
                    chatMemory.add(SystemMessage.from(systemPrompt))
                }
                for (msg in ctx.recentHistory) {
                    when (msg.senderType) {
                        SenderType.USER -> chatMemory.add(UserMessage.from(msg.getPlainText()))
                        SenderType.CHARACTER -> chatMemory.add(AiMessage.from(msg.getPlainText()))
                    }
                }

                // Build AiServices
                val builder = AiServices.builder(ChatAssistant::class.java)
                    .streamingChatModel(streamingModel)
                    .chatMemory(chatMemory)

                // Register @Tool annotated objects
                if (ctx.toolObjects.isNotEmpty()) {
                    builder.tools(ctx.toolObjects)
                }

                // Register dynamic plugin tools via toolProvider
                if (ctx.pluginTools.isNotEmpty()) {
                    val pluginToolsCopy = ctx.pluginTools.toMap()
                    builder.toolProvider { _ ->
                        dev.langchain4j.service.tool.ToolProviderResult.builder()
                            .apply {
                                for ((spec, executor) in pluginToolsCopy) {
                                    add(spec, executor)
                                }
                            }
                            .build()
                    }
                }

                val assistant = builder.build()

                // Execute chat
                logger.info { "[$requestId] 开始执行LLM对话..." }
                val startTime = System.currentTimeMillis()
                val futureResponse = CompletableFuture<ChatResponse>()
                val fullContent = StringBuilder()
                var toolCallCount = 0

                val tokenStream = assistant.chat(userText)

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
                        futureResponse.complete(response)
                    }
                    .onError { error ->
                        futureResponse.completeExceptionally(error)
                    }
                    .start()

                // Wait for completion
                val response = futureResponse.join()
                val executionTime = System.currentTimeMillis() - startTime
                val result = fullContent.toString().ifEmpty {
                    response.aiMessage().text() ?: ""
                }
                logger.info { "[$requestId] LLM对话完成: executionTime=${executionTime}ms, resultLength=${result.length}" }

                // Save assistant message
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
                activeChats.remove(chatId)
            }
        }
    }
}
