# 工作计划：Embedding 重试机制与自动处理 PENDING 文件

## TL;DR

为知识库文件处理添加两个核心功能：
1. **Embedding 重试机制**：失败时自动重试 3 次，指数退避
2. **自动处理 PENDING 文件**：后台任务定期扫描并处理待处理文件

## 需求确认

### 功能 1：Embedding 重试机制
- **目标**：在 embedding 调用失败时自动重试
- **重试次数**：最多 3 次
- **退避策略**：指数退避（1s, 2s, 4s）
- **触发场景**：网络超时、模型服务暂时不可用等

### 功能 2：自动处理 PENDING 文件
- **目标**：定期扫描并自动处理处于 PENDING 状态的文件
- **执行频率**：每 30 秒
- **处理逻辑**：调用现有的 `processFile` 方法
- **并发控制**：最多 5 个并发
- **重试限制**：一个文件最多自动处理 3 次，超过后标记为 FAILED
- **锁机制**：当没有正在处理的文件时才开始检测，避免重复扫描

## 技术方案

### 方案 A：简单改进（推荐快速实现）

**Task 1: 添加 Embedding 重试**
```kotlin
// 在 KnowledgeFileService 中添加
private suspend fun embedWithRetry(text: String, maxRetries: Int = 3): FloatArray {
    var lastException: Exception? = null
    repeat(maxRetries) { attempt ->
        try {
            return embeddingService.embed(text)
        } catch (e: Exception) {
            lastException = e
            if (attempt < maxRetries - 1) {
                logger.warn { "Embedding 失败，${attempt + 1}/$maxRetries 次尝试，${1000 * (attempt + 1)}ms 后重试: ${e.message}" }
                delay(1000L * (attempt + 1))
            }
        }
    }
    throw lastException ?: Exception("Embedding 失败，已重试 $maxRetries 次")
}
```

**Task 2: 创建后台处理任务**
```kotlin
// 创建 PendingFileProcessor
class PendingFileProcessor(
    private val fileService: KnowledgeFileUseCasePort,
    private val fileRepository: KnowledgeFileRepositoryPort,
    private val scope: CoroutineScope,
) {
    fun start() {
        scope.launch {
            while (isActive) {
                processPendingFiles()
                delay(30_000) // 30 秒间隔
            }
        }
    }
    
    private suspend fun processPendingFiles() {
        val pendingFiles = fileRepository.findByStatus(FileStatus.PENDING)
        pendingFiles.forEach { file ->
            try {
                fileService.processFile(file.id)
            } catch (e: Exception) {
                logger.error(e) { "自动处理文件 ${file.id} 失败" }
            }
        }
    }
}
```

### 方案 B：完整重试框架（长期）
- 使用 Resilience4j 或类似库
- 可配置的退避策略
- 重试次数和间隔配置化
- 支持断路器模式

## 推荐方案

**采用方案 A（简单实现）**，因为：
1. 改动小，风险低
2. 满足当前需求
3. 易于理解和维护
4. 后续可升级为方案 B

## 任务分解

### Task 1: Embedding 重试机制（30 分钟）
**文件**：`KnowledgeFileService.kt`

**修改内容**：
1. 添加 `embedWithRetry` 私有方法
2. 修改 `processFile` 中的 embedding 调用
3. 添加必要的 import（delay）

**测试验证**：
- 模拟 embedding 失败，观察重试日志
- 验证 3 次后抛出异常
- 验证退避时间间隔

### Task 2: Repository 扩展 - 添加重试次数字段（20 分钟）
**文件**：
- `V1__init.sql`（修改，添加 retry_count 字段）
- `KnowledgeFile.kt`（修改，添加 retryCount 属性）
- `KnowledgeFileTable.kt`（修改，添加 retry_count 列）
- `ExposedKnowledgeFileRepository.kt`（修改，添加重试计数更新逻辑）

**数据库变更**：
```sql
ALTER TABLE knowledge_files ADD COLUMN IF NOT EXISTS retry_count INT DEFAULT 0;
```

**修改内容**：
1. 在 domain 模型中添加 `retryCount` 字段
2. 在 Exposed Table 中添加列
3. 在 Repository 中添加 `incrementRetryCount` 方法
4. 添加 `findByStatusAndRetryLessThan(status, maxRetries)` 方法

### Task 3: 创建 PendingFileProcessor（60 分钟）
**文件**：
- `PendingFileProcessor.kt`（新建）

**核心设计**：
1. **锁机制**：使用 `AtomicBoolean` 作为锁，确保只有一个检测线程运行
2. **并发控制**：使用 Semaphore(5) 限制最多 5 个并发
3. **重试计数**：处理前检查 retry_count，超过 3 次标记为 FAILED
4. **空闲检测**：只有当没有正在处理的文件时才扫描

```kotlin
class PendingFileProcessor(
    private val fileService: KnowledgeFileUseCasePort,
    private val fileRepository: KnowledgeFileRepositoryPort,
    private val scope: CoroutineScope,
) {
    private val isProcessing = AtomicBoolean(false)
    private val semaphore = Semaphore(5)
    private val maxRetries = 3
    
    fun start() {
        scope.launch {
            while (isActive) {
                delay(30_000)
                // 只有空闲时才检测
                if (isProcessing.compareAndSet(false, true)) {
                    try {
                        processPendingFiles()
                    } finally {
                        isProcessing.set(false)
                    }
                }
            }
        }
    }
    
    private suspend fun processPendingFiles() {
        val pendingFiles = fileRepository.findByStatusAndRetryLessThan(
            FileStatus.PENDING, 
            maxRetries
        )
        
        pendingFiles.forEach { file ->
            semaphore.acquire()
            scope.launch {
                try {
                    // 先增加重试计数
                    fileRepository.incrementRetryCount(file.id)
                    fileService.processFile(file.id)
                } catch (e: Exception) {
                    logger.error(e) { "自动处理文件 ${file.id} 失败（第 ${file.retryCount + 1} 次）" }
                    
                    // 检查是否超过最大重试次数
                    if (file.retryCount + 1 >= maxRetries) {
                        tx.execute {
                            val f = fileRepository.findById(file.id) ?: return@execute
                            f.markFailed("自动处理失败 ${maxRetries} 次: ${e.message}")
                            fileRepository.save(f)
                        }
                    }
                } finally {
                    semaphore.release()
                }
            }
        }
    }
}
```

### Task 4: 集成到系统（20 分钟）
**文件**：
- `KnowledgeModule.kt`（修改）
- `Application.kt`（修改，启动处理器）

**步骤**：
1. 在 KnowledgeModule 中注册 PendingFileProcessor
2. 在应用启动时启动处理器
3. 添加配置项（是否启用、间隔时间、最大并发）
4. 添加优雅关闭逻辑

**测试验证**：
- 上传文件后，观察自动处理
- 验证 PENDING → PROCESSING → COMPLETED 状态流转
- 验证失败时状态变为 FAILED

### Task 3: 监控与日志（20 分钟）
**内容**：
- 添加处理统计日志（每轮处理数量、成功率）
- 添加 metrics（可选）
- 添加优雅关闭（应用关闭时停止处理器）

## 执行顺序

```
Task 1 (Embedding 重试)
    ↓
Task 2 (Repository 扩展 - 添加重试次数字段)
    ↓
Task 3 (PendingFileProcessor 实现)
    ↓
Task 4 (集成到系统)
```

## 风险提示

1. **并发处理**：同时处理多个文件可能导致资源竞争
   - **缓解**：限制并发数，使用 semaphore

2. **无限重试**：如果某文件一直失败，会不断重试
   - **缓解**：添加最大重试次数限制（如最多自动处理 3 次）

3. **重复处理**：处理器和手动调用可能同时执行
   - **缓解**：处理前检查状态，确保只有 PENDING 才处理

## 配置建议

```yaml
knowledge:
  pending-file-processor:
    enabled: true
    interval-seconds: 30
    batch-size: 5  # 每轮最多处理 5 个
  embedding:
    max-retries: 3
    retry-delay-ms: 1000
```

## 成功标准

- [ ] **Task 1**: Embedding 失败时自动重试 3 次，间隔为 1s、2s、4s
- [ ] **Task 2**: 
  - 数据库表添加 retry_count 字段
  - Repository 支持重试计数查询和更新
- [ ] **Task 3**: 
  - 锁机制工作正常（不会同时有多个扫描线程）
  - 并发限制为 5 个
  - 一个文件最多自动处理 3 次
  - 超过 3 次自动标记为 FAILED
- [ ] **Task 4**: 
  - 处理器随应用启动和关闭
  - 30 秒间隔执行
  - PENDING 文件被自动处理
- [ ] 日志中包含处理统计信息
- [ ] 应用关闭时处理器优雅停止

## 下一步

请确认：
1. 这个计划是否符合你的预期？
2. 处理间隔 30 秒是否合适？
3. 是否需要限制并发处理数量？
4. 是否需要添加最大自动重试次数（如一个文件最多自动处理 3 次）？

确认后，我将开始执行 Task 1。