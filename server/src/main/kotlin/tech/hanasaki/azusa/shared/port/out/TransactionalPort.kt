package tech.hanasaki.azusa.shared.port.out

interface TransactionalPort {
    suspend fun <T> execute(block: suspend () -> T): T
    suspend fun <T> readOnly(block: suspend () -> T): T
}