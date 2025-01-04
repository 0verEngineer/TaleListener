package org.overengineer.talelistener.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.sqliter.DatabaseConfiguration
import org.overengineer.Database

actual class DriverFactory {
    private lateinit var driver: SqlDriver

    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = Database.Schema,
            name ="talelistener.db",
            onConfiguration = { config: DatabaseConfiguration ->
                config.copy(
                    extendedConfig = DatabaseConfiguration.Extended(foreignKeyConstraints = true)
                )
            }
        )
    }

    actual fun getDriver(): SqlDriver = driver
}