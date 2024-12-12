package org.overengineer.talelistener.db

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import org.overengineer.Database
import org.overengineer.talelistener.channel.common.LibraryType
import org.overengineer.talelistener.sqldelight.Libraries

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory): Database {
    val driver = driverFactory.createDriver()
    return Database(
        driver = driver,
        librariesAdapter = Libraries.Adapter(EnumColumnAdapter<LibraryType>())
    )
}
