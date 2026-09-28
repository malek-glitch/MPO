package tn.pcecom.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import tn.pcecom.config.DbConfig

object DatabaseFactory {
    fun init(cfg: DbConfig) {
        val dataSource = HikariDataSource(HikariConfig().apply {
            jdbcUrl = cfg.url
            username = cfg.user
            password = cfg.password
            maximumPoolSize = 10
        })
        Flyway.configure().dataSource(dataSource).load().migrate()
        Database.connect(dataSource)
    }
}

/** Runs a blocking Exposed transaction on the IO dispatcher. */
suspend fun <T> dbQuery(block: Transaction.() -> T): T =
    withContext(Dispatchers.IO) { transaction { block() } }
