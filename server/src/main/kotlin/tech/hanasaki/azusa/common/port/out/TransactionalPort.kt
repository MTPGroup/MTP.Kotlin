package tech.hanasaki.azusa.common.port.out

interface TransactionalPort {
    suspend fun <T> execute(block: suspend () -> T): T
    suspend fun <T> readOnly(block: suspend () -> T): T
}