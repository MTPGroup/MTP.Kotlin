package tech.hanasaki.azusa.modules.knowledge.domain.model

/**
 * 文件处理状态
 */
enum class FileStatus {
    PENDING,     // 待处理
    PROCESSING,  // 处理中
    COMPLETED,   // 完成
    FAILED;      // 失败
}
