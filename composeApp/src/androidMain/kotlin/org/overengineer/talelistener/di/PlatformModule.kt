package org.overengineer.talelistener.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.overengineer.talelistener.db.DriverFactory
import org.overengineer.talelistener.platform.AudioPlayerAndroid
import org.overengineer.talelistener.platform.NetworkQualityService
import org.overengineer.talelistener.playback.createExoPlayer
import org.overengineer.talelistener.playback.createMediaSession
import org.overengineer.talelistener.playback.service.PlaybackSynchronizationServiceAndroid
import org.overengineer.talelistener.ui.viewmodel.PlayerViewModel

actual fun platformModule() = module {
    single { DriverFactory(androidContext()) }
    single { NetworkQualityService(androidContext(), get(), get()) }
    single { createExoPlayer(androidContext()) }
    single { createMediaSession(androidContext(), get()) }
    single { PlaybackSynchronizationServiceAndroid(get(), get(), get()) }

    // We create the PlayerViewModel at start because we want the MediaController of AudioPlayerAndroid initialized at the start
    single(createdAtStart = true) { PlayerViewModel(AudioPlayerAndroid(androidContext(), get(), get())) }
}