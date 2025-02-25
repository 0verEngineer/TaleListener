package org.overengineer.talelistener.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.github.aakira.napier.Napier
import org.overengineer.Database
import java.io.File
import java.util.Properties

actual class DriverFactory {

    private lateinit var driver: SqlDriver

    actual fun createDriver(): SqlDriver {
        val dbPath = getDatabasePath()
        Napier.d("dbPath: $dbPath")
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:$dbPath",
            properties = Properties().apply { put("foreign_keys", "true") }
        )
        if (!File(dbPath).exists()) {
            Database.Schema.create(driver)
        }
        this.driver = driver
        return driver
    }

    actual fun getDriver(): SqlDriver = driver

    private fun getDatabasePath(): String {
        val appName = "TaleListener"

        val isDev = System.getenv("IS_DEV")?.toBoolean() ?: false

        Napier.d("IS_DEV: $isDev")

        return if (isDev) {
            val projectDir = System.getProperty("user.dir") ?: "."
            "$projectDir/devData/database.db"
        } else {
            val homeDir = System.getProperty("user.home")
            val osName = System.getProperty("os.name").lowercase()
            var result = "$homeDir/.config/$appName/database.db"
            if (osName.contains("win")) {
                result = "${System.getenv("APPDATA")}/$appName/database.db"
            }
            else if (osName.contains("mac")) {
                result = "$homeDir/Library/Application Support/$appName/database.db"
            }

            result
        }.also { path ->
            File(path).parentFile.mkdirs()
        }
    }
}