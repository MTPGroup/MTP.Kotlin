package tech.hanasaki.azusa.shared.infrastructure.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun <T> dbQuery(block: suspend () -> T): T =
    withContext(Dispatchers.IO) { block() }
