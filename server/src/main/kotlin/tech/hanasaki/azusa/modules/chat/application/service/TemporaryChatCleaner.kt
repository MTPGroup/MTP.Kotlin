package tech.hanasaki.azusa.modules.chat.application.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import tech.hanasaki.azusa.modules.chat.domain.port.ChatRepositoryPort
import tech.hanasaki.azusa.shared.port.out.TransactionalPort

private val logger = KotlinLogging.logger {}

class TemporaryChatCleaner(
    private val chatRepository: ChatRepositoryPort,
    private val tx: TransactionalPort,
    private val scope: CoroutineScope,
    private val intervalMs: Long = 600_000, // 10 minutes
) {
    fun start() {
        logger.info { "TemporaryChatCleaner 启动，间隔: ${intervalMs}ms" }

        scope.launch {
            while (isActive) {
                delay(intervalMs)
                try {
                    val deleted = tx.execute { chatRepository.deleteExpiredTemporaryChats() }
                    if (deleted > 0) {
                        logger.info { "清理了 $deleted 个过期临时会话" }
                    }
                } catch (e: Exception) {
                    logger.error(e) { "清理过期临时会话时发生错误" }
                }
            }
        }
    }
}
