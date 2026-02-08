package tech.hanasaki.azusa.modules.knowledge.application.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import tech.hanasaki.azusa.modules.knowledge.application.port.`in`.KnowledgeFileUseCasePort
import tech.hanasaki.azusa.modules.knowledge.domain.model.FileStatus
import tech.hanasaki.azusa.modules.knowledge.domain.port.KnowledgeFileRepositoryPort
import tech.hanasaki.azusa.shared.port.out.TransactionalPort
import java.util.concurrent.atomic.AtomicBoolean

private val logger = KotlinLogging.logger {}

class PendingFileProcessor(
    private val fileService: KnowledgeFileUseCasePort,
    private val fileRepository: KnowledgeFileRepositoryPort,
    private val tx: TransactionalPort,
    private val scope: CoroutineScope,
    private val config: Config = Config(),
) {
    data class Config(
        val enabled: Boolean = true,
        val intervalMs: Long = 30_000,
        val maxConcurrency: Int = 5,
        val maxRetries: Int = 3,
        val batchSize: Int = 10,
    )

    private val isProcessing = AtomicBoolean(false)
    private val semaphore = Semaphore(config.maxConcurrency)

    fun start() {
        if (!config.enabled) {
            logger.info { "PendingFileProcessor 已禁用" }
            return
        }

        logger.info { "PendingFileProcessor 启动，间隔: ${config.intervalMs}ms, 最大并发: ${config.maxConcurrency}, 最大重试: ${config.maxRetries}" }

        scope.launch {
            while (isActive) {
                delay(config.intervalMs)

                if (isProcessing.compareAndSet(false, true)) {
                    try {
                        processPendingFiles()
                    } catch (e: Exception) {
                        logger.error(e) { "处理待处理文件时发生错误" }
                    } finally {
                        isProcessing.set(false)
                    }
                } else {
                    logger.debug { "上一轮处理尚未完成，跳过本次扫描" }
                }
            }
        }
    }

    private suspend fun processPendingFiles() =
        tx.execute {
            val pendingFiles = fileRepository.findByStatusAndRetryLessThan(
                status = FileStatus.PENDING,
                maxRetries = config.maxRetries,
                limit = config.batchSize
            )

            if (pendingFiles.isEmpty()) {
                return@execute
            }

            logger.info { "发现 ${pendingFiles.size} 个待处理文件" }

            var successCount = 0
            var failCount = 0

            pendingFiles.forEach { file ->
                semaphore.acquire()
                scope.launch {
                    try {
                        tx.execute {
                            val f = fileRepository.findById(file.id) ?: return@execute
                            f.incrementRetry()
                            fileRepository.save(f)
                        }

                        fileService.processFile(file.id)
                        successCount++
                        logger.info { "文件 ${file.id} 处理成功" }
                    } catch (e: Exception) {
                        failCount++
                        logger.error(e) { "文件 ${file.id} 处理失败（第 ${file.retryCount + 1}/${config.maxRetries} 次）" }

                        if (file.retryCount + 1 >= config.maxRetries) {
                            try {
                                tx.execute {
                                    val f = fileRepository.findById(file.id) ?: return@execute
                                    f.markFailed("自动处理失败 ${config.maxRetries} 次: ${e.message}")
                                    fileRepository.save(f)
                                }
                                logger.warn { "文件 ${file.id} 已达到最大重试次数，标记为失败" }
                            } catch (inner: Exception) {
                                logger.error(inner) { "标记文件 ${file.id} 为失败状态时出错" }
                            }
                        }
                    } finally {
                        semaphore.release()
                    }

                    repeat(pendingFiles.size) {
                        semaphore.acquire()
                    }
                    repeat(pendingFiles.size) {
                        semaphore.release()
                    }

                    logger.info { "本轮处理完成: 成功 $successCount, 失败 $failCount" }
                }
            }
        }
}