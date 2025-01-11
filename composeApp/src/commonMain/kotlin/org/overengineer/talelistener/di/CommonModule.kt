package org.overengineer.talelistener.di

import org.koin.dsl.module
import org.overengineer.talelistener.channel.audiobookshelf.AudiobookshelfChannelProvider
import org.overengineer.talelistener.channel.audiobookshelf.common.UnknownAudiobookshelfChannel
import org.overengineer.talelistener.channel.audiobookshelf.common.api.AudioBookshelfDataRepository
import org.overengineer.talelistener.channel.audiobookshelf.common.api.AudioBookshelfMediaRepository
import org.overengineer.talelistener.channel.audiobookshelf.common.api.AudioBookshelfSyncService
import org.overengineer.talelistener.channel.audiobookshelf.common.api.AudiobookshelfAuthService
import org.overengineer.talelistener.channel.audiobookshelf.library.LibraryAudiobookshelfChannel
import org.overengineer.talelistener.channel.audiobookshelf.podcast.PodcastAudiobookshelfChannel
import org.overengineer.talelistener.content.TLMediaProvider
import org.overengineer.talelistener.content.cache.LocalCacheRepository
import org.overengineer.talelistener.content.cache.api.CachedBookRepository
import org.overengineer.talelistener.content.cache.api.CachedLibraryRepository
import org.overengineer.talelistener.db.DBHolder
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences
import org.overengineer.talelistener.ui.viewmodel.CachingViewModel
import org.overengineer.talelistener.ui.viewmodel.LibraryViewModel
import org.overengineer.talelistener.ui.viewmodel.LoginViewModel
import org.overengineer.talelistener.ui.viewmodel.PlayerViewModel
import org.overengineer.talelistener.ui.viewmodel.SettingsViewModel
import org.overengineer.talelistener.ui.viewmodel.SplashScreenViewModel

fun commonModule() = module {
    single { DBHolder(get()) }

    single { TaleListenerSharedPreferences() }

    single { AudioBookshelfDataRepository(get()) }
    single { AudioBookshelfMediaRepository(get()) }

    single { AudioBookshelfSyncService(get()) }

    single { AudioBookshelfSyncService(get()) }

    single { AudiobookshelfAuthService(get()) }

    single { UnknownAudiobookshelfChannel(
        get(),
        get(),
        get(),
        get(),
    ) }

    single { LibraryAudiobookshelfChannel(
        get(),
        get(),
        get(),
        get(),
    ) }

    single { PodcastAudiobookshelfChannel(
        get(),
        get(),
        get(),
        get(),
    ) }

    single { AudiobookshelfChannelProvider(
        get(),
        get(),
        get(),
        get(),
        get()
    ) }

    single { CachedBookRepository(get(), get()) }
    single { CachedLibraryRepository(get()) }

    single { LocalCacheRepository(get(), get()) }
    single { TLMediaProvider(
        get(),
        get(),
        get()
    ) }

    single { LoginViewModel(get(), get()) }
    single { SettingsViewModel(get(), get()) }
    single { SplashScreenViewModel(get(), get()) }
    single { LibraryViewModel(get(), get()) }
    single { CachingViewModel(get(), get()) }
    single { PlayerViewModel(get(), get()) }
}
