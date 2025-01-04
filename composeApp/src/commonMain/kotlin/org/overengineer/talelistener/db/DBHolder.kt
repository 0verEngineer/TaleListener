package org.overengineer.talelistener.db

import app.cash.sqldelight.db.SqlDriver


class DBHolder(
    private val driverFactory: DriverFactory
) {
    val db = createDatabase(driverFactory)
    val bookQueries = db.bookQueries
    val libraryQueries = db.libraryQueries

    fun getDriver(): SqlDriver = driverFactory.getDriver()
}
