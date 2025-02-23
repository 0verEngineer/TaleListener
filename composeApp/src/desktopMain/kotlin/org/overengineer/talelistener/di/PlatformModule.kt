package org.overengineer.talelistener.di

import org.koin.dsl.module
import org.overengineer.talelistener.db.DriverFactory
import org.overengineer.talelistener.platform.AudioPlayerDesktop
import org.overengineer.talelistener.platform.NetworkQualityService
import org.overengineer.talelistener.ui.viewmodel.PlayerViewModel

actual fun platformModule() = module {
    single { DriverFactory() }
    single { NetworkQualityService(get(), get()) }
    single { AudioPlayerDesktop(get(), get()) }
    single { PlayerViewModel(get<AudioPlayerDesktop>()) }
}
