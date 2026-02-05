package tech.hanasaki.azusa.common.adapter.out.persistence

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import tech.hanasaki.azusa.common.port.out.TransactionalPort

class ExposedTransactionAdapter : TransactionalPort {
    override suspend fun <T> execute(block: suspend () -> T): T =
        withContext(Dispatchers.IO) {
            suspendTransaction { block() }
        }

    override suspend fun <T> readOnly(block: suspend () -> T): T =
        withContext(Dispatchers.IO) {
            suspendTransaction(readOnly = true) {
                block()
            }
        }
}