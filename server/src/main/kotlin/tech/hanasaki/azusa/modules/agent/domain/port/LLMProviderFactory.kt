package tech.hanasaki.azusa.modules.agent.domain.port

import tech.hanasaki.azusa.common.domain.exception.DomainException

/**
 * LLM 提供者工厂
 * 根据提供者类型创建对应的 LLMProvider 实例
 */
interface LLMProviderFactory {
    /**
     * 根据提供者名称创建 LLMProvider 实例
     *
     * @param providerName LLM 提供者名称，如 "openai", "anthropic" 等
     * @return LLMProvider 实例
     * @throws DomainException 当提供者类型不支持时
     */
    fun create(providerName: String): LLMProvider
}
