package com.francoherrero.ai_agent_multiplatform.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import java.sql.Connection

object DatabaseFactory {

    fun init() {
        val dataSource = hikari()
        Database.connect(dataSource)

        // Recommended for safety in server apps
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_READ_COMMITTED
    }

    private fun hikari(): HikariDataSource {
        val host = env("DB_HOST")
        val port = env("DB_PORT", "5432")
        val db   = env("DB_NAME", "postgres")
        val user = env("DB_USER")
        val pass = env("DB_PASS")
        val ssl  = env("DB_SSL", "true").toBoolean()

        val jdbcUrl = buildString {
            append("jdbc:postgresql://")
            append(host).append(":").append(port).append("/").append(db)
            if (ssl) append("?sslmode=require")
        }

        val config = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            this.username = user
            this.password = pass
            this.maximumPoolSize = 5
            this.minimumIdle = 1
            this.isAutoCommit = false
            this.transactionIsolation = "TRANSACTION_READ_COMMITTED"
            this.validate()
        }
        return HikariDataSource(config)
    }

    private fun env(key: String, default: String? = null): String {
        return System.getenv(key) ?: default ?: error("Missing env var: $key")
    }
}
