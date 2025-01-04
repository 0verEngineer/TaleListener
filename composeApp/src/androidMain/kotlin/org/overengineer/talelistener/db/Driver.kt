package org.overengineer.talelistener.db

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.overengineer.Database

actual class DriverFactory(private val context: Context) {

    private lateinit var driver: SqlDriver

    actual fun createDriver(): SqlDriver {
        driver = AndroidSqliteDriver(Database.Schema, context, "talelistener.db",
            callback = object : AndroidSqliteDriver.Callback(Database.Schema) {
                override fun onConfigure(db: SupportSQLiteDatabase) {
                    db.setForeignKeyConstraintsEnabled(true)
                }
            })

        return driver
    }

    actual fun getDriver(): SqlDriver = driver
}