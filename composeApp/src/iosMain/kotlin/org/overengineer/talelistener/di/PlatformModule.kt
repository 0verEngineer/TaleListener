package org.overengineer.talelistener.di

import org.koin.dsl.module
import org.overengineer.talelistener.db.DriverFactory
import org.overengineer.talelistener.platform.NetworkQualityService

actual fun platformModule() = module {
    single { DriverFactory() }
    single { NetworkQualityService(get(), get()) }
}
