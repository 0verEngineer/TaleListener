package org.overengineer.talelistener.di

import org.koin.dsl.module
import org.overengineer.talelistener.channel.audiobookshelf.AudiobookshelfChannelProvider
import org.overengineer.talelistener.channel.audiobookshelf.common.UnknownAudiobookshelfChannel
import org.overengineer.talelistener.channel.audiobookshelf.common.api.AudioBookshelfDataRepository
import org.overengineer.talelistener.channel.audiobookshelf.common.api.AudioBookshelfMediaRepository
import org.overengineer.talelistener.channel.audiobookshelf.common.api.AudioBookshelfSyncService
import org.overengineer.talelistener.channel.audiobookshelf.common.api.AudiobookshelfAuthService
import org.overengineer.talelistener.channel.audiobookshelf.common.api.RequestHeadersProvider
import org.overengineer.talelistener.channel.audiobookshelf.common.converter.ConnectionInfoResponseConverter
import org.overengineer.talelistener.channel.audiobookshelf.common.converter.LibraryPageResponseConverter
import org.overengineer.talelistener.channel.audiobookshelf.common.converter.LibraryResponseConverter
import org.overengineer.talelistener.channel.audiobookshelf.common.converter.LoginResponseConverter
import org.overengineer.talelistener.channel.audiobookshelf.common.converter.PlaybackSessionResponseConverter
import org.overengineer.talelistener.channel.audiobookshelf.common.converter.RecentListeningResponseConverter
import org.overengineer.talelistener.channel.audiobookshelf.library.LibraryAudiobookshelfChannel
import org.overengineer.talelistener.channel.audiobookshelf.library.converter.BookResponseConverter
import org.overengineer.talelistener.channel.audiobookshelf.library.converter.LibrarySearchItemsConverter
import org.overengineer.talelistener.channel.audiobookshelf.podcast.PodcastAudiobookshelfChannel
import org.overengineer.talelistener.channel.audiobookshelf.podcast.converter.PodcastPageResponseConverter
import org.overengineer.talelistener.channel.audiobookshelf.podcast.converter.PodcastResponseConverter
import org.overengineer.talelistener.channel.audiobookshelf.podcast.converter.PodcastSearchItemsConverter
import org.overengineer.talelistener.content.LissenMediaProvider
import org.overengineer.talelistener.content.cache.LocalCacheRepository
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences
import org.overengineer.talelistener.ui.viewmodel.LoginViewModel
import org.overengineer.talelistener.ui.viewmodel.SettingsScreenViewModel
import org.overengineer.talelistener.ui.viewmodel.SplashScreenViewModel

fun commonModule() = module {
    single { TaleListenerSharedPreferences() }

    single { RequestHeadersProvider(get()) }

    single { AudioBookshelfDataRepository(get(), get()) }
    single { AudioBookshelfMediaRepository(get(), get()) }

    single { RecentListeningResponseConverter() }
    single { AudioBookshelfSyncService(get()) }
    single { PlaybackSessionResponseConverter() }
    single { LibraryResponseConverter() }
    single { LibraryPageResponseConverter() }
    single { LibrarySearchItemsConverter() }
    single { ConnectionInfoResponseConverter() }
    single { PodcastPageResponseConverter() }
    single { PodcastResponseConverter() }
    single { PodcastSearchItemsConverter() }
    single { BookResponseConverter() }
    single { LoginResponseConverter() }

    single { AudioBookshelfSyncService(get()) }

    single { AudiobookshelfAuthService(get(), get()) }

    single { UnknownAudiobookshelfChannel(
        get(),
        get(),
        get(),
        get(),
        get(),
        get(),
        get(),
        get()
    ) }

    single { LibraryAudiobookshelfChannel(
        get(),
        get(),
        get(),
        get(),
        get(),
        get(),
        get(),
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
        get(),
        get(),
        get(),
        get(),
        get(),
        get(),
        get()
    ) }

    single { AudiobookshelfChannelProvider(
        get(),
        get(),
        get(),
        get(),
        get()
    ) }

    single { LocalCacheRepository() }
    single { LissenMediaProvider(
        get(),
        get(),
        get()
    ) }

    single { LoginViewModel(get(), get()) }
    single { SettingsScreenViewModel() }
    single { SplashScreenViewModel(get()) }
}
