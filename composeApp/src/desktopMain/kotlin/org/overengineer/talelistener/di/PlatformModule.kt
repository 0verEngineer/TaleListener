package org.overengineer.talelistener.di

import org.koin.dsl.module
import org.overengineer.talelistener.db.DriverFactory

actual fun platformModule() = module {
    single { DriverFactory() }
}
