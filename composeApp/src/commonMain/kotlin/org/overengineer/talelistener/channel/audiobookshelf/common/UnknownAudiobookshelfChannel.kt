
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.audiobookshelf.common
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Removed javax singleton stuff
 * - Use TaleListenerSharedPreferences
 */

package org.overengineer.talelistener.channel.audiobookshelf.common

import org.overengineer.talelistener.channel.audiobookshelf.common.api.AudioBookshelfDataRepository
import org.overengineer.talelistener.channel.audiobookshelf.common.api.AudioBookshelfMediaRepository
import org.overengineer.talelistener.channel.audiobookshelf.common.api.AudioBookshelfSyncService
import org.overengineer.talelistener.channel.audiobookshelf.common.converter.ConnectionInfoResponseConverter
import org.overengineer.talelistener.channel.audiobookshelf.common.converter.LibraryResponseConverter
import org.overengineer.talelistener.channel.audiobookshelf.common.converter.PlaybackSessionResponseConverter
import org.overengineer.talelistener.channel.audiobookshelf.common.converter.RecentListeningResponseConverter
import org.overengineer.talelistener.channel.common.ApiError
import org.overengineer.talelistener.channel.common.ApiResult
import org.overengineer.talelistener.channel.common.LibraryType
import org.overengineer.talelistener.domain.Book
import org.overengineer.talelistener.domain.DetailedItem
import org.overengineer.talelistener.domain.PagedItems
import org.overengineer.talelistener.domain.PlaybackSession
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences

class UnknownAudiobookshelfChannel constructor(
    dataRepository: AudioBookshelfDataRepository,
    mediaRepository: AudioBookshelfMediaRepository,
    recentListeningResponseConverter: RecentListeningResponseConverter,
    preferences: TaleListenerSharedPreferences,
    syncService: AudioBookshelfSyncService,
    sessionResponseConverter: PlaybackSessionResponseConverter,
    libraryResponseConverter: LibraryResponseConverter,
    connectionInfoResponseConverter: ConnectionInfoResponseConverter,
) : AudiobookshelfChannel(
    dataRepository = dataRepository,
    mediaRepository = mediaRepository,
    recentBookResponseConverter = recentListeningResponseConverter,
    sessionResponseConverter = sessionResponseConverter,
    preferences = preferences,
    syncService = syncService,
    libraryResponseConverter = libraryResponseConverter,
    connectionInfoResponseConverter = connectionInfoResponseConverter,
) {

    override fun getLibraryType(): LibraryType = LibraryType.UNKNOWN

    override suspend fun fetchBooks(
        libraryId: String,
        pageSize: Int,
        pageNumber: Int,
    ): ApiResult<PagedItems<Book>> = ApiResult.Error(ApiError.UnsupportedError)

    override suspend fun searchBooks(
        libraryId: String,
        query: String,
        limit: Int,
    ): ApiResult<List<Book>> = ApiResult.Error(ApiError.UnsupportedError)

    override suspend fun startPlayback(
        bookId: String,
        episodeId: String,
        supportedMimeTypes: List<String>,
        deviceId: String,
    ): ApiResult<PlaybackSession> = ApiResult.Error(ApiError.UnsupportedError)

    override suspend fun fetchBook(
        bookId: String,
    ): ApiResult<DetailedItem> = ApiResult.Error(ApiError.UnsupportedError)
}
