package tech.hanasaki.azusa.common.platform.event

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.ScriptOutputType
import io.lettuce.core.SetArgs
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import java.util.*

@OptIn(ExperimentalLettuceCoroutinesApi::class)
class LettuceDistLock(
    private val redis: RedisCoroutinesCommands<String, String>,
    private val lockKey: String,
    private val lockTimeoutMs: Long = 5000,
) {
    private val lockValue = UUID.randomUUID().toString()

    suspend fun tryLock(): Boolean {
        val args = SetArgs().nx().px(lockTimeoutMs)
        val result = redis.set(lockKey, lockValue, args)
        return result == "OK"
    }

    suspend fun unlock() {
        val script = """
            if redis.call("get", KEYS[1]) == ARGV[1] then
                return redis.call("del", KEYS[1])
            else
                return 0
            end
        """.trimIndent()
        redis.eval<Long>(
            script,
            ScriptOutputType.INTEGER,
            arrayOf(lockKey),
            lockValue
        )
    }
}