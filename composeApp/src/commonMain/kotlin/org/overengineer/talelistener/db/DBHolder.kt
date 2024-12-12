package org.overengineer.talelistener.db


class DBHolder(
    private val driverFactory: DriverFactory
) {
    val db = createDatabase(driverFactory)
}
