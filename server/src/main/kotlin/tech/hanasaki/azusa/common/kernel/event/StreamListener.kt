package tech.hanasaki.azusa.common.kernel.event

interface StreamListener {
    suspend fun onMessage(): Boolean
    suspend fun start()
}