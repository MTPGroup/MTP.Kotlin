package tech.hanasaki.azusa.shared.port.out

interface EventPublisherPort {
    suspend fun publish(eventType: String, payload: String)
}
