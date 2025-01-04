package org.overengineer.talelistener.db

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import org.overengineer.Database
import org.overengineer.talelistener.channel.common.LibraryType
import org.overengineer.talelistener.sqldelight.LibraryEntity

expect class DriverFactory {
    fun createDriver(): SqlDriver
    fun getDriver(): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory): Database {
    val driver = driverFactory.createDriver()
    return Database(
        driver = driver,
        libraryEntityAdapter = LibraryEntity.Adapter(EnumColumnAdapter<LibraryType>())
    )
}
