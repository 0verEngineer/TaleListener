
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.audiobookshelf.library.model
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Removed javax singleton stuff
 * - Use TaleListenerSharedPreferences
 */

package org.overengineer.talelistener.channel.audiobookshelf.library

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.overengineer.talelistener.channel.audiobookshelf.common.AudiobookshelfChannel
import org.overengineer.talelistener.channel.audiobookshelf.common.api.AudioBookshelfDataRepository
import org.overengineer.talelistener.channel.audiobookshelf.common.api.AudioBookshelfMediaRepository
import org.overengineer.talelistener.channel.audiobookshelf.common.api.AudioBookshelfSyncService
import org.overengineer.talelistener.channel.audiobookshelf.common.model.playback.DeviceInfo
import org.overengineer.talelistener.channel.audiobookshelf.common.model.playback.PlaybackStartRequest
import org.overengineer.talelistener.channel.common.ApiResult
import org.overengineer.talelistener.channel.common.ApiResult.Success
import org.overengineer.talelistener.channel.common.LibraryType
import org.overengineer.talelistener.content.cache.converter.bookResponseAndMediaProgressResponseToDetailedItem
import org.overengineer.talelistener.content.cache.converter.libraryItemListToBookList
import org.overengineer.talelistener.content.cache.converter.libraryPageResponseToPagedBooks
import org.overengineer.talelistener.content.cache.converter.playbackSessionResponseToPlaybackSession
import org.overengineer.talelistener.domain.Book
import org.overengineer.talelistener.domain.DetailedItem
import org.overengineer.talelistener.domain.PagedItems
import org.overengineer.talelistener.domain.PlaybackSession
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences

class LibraryAudiobookshelfChannel (
    dataRepository: AudioBookshelfDataRepository,
    mediaRepository: AudioBookshelfMediaRepository,
    preferences: TaleListenerSharedPreferences,
    syncService: AudioBookshelfSyncService,
) : AudiobookshelfChannel(
    dataRepository = dataRepository,
    mediaRepository = mediaRepository,
    preferences = preferences,
    syncService = syncService,
) {

    override fun getLibraryType() = LibraryType.LIBRARY

    override suspend fun fetchBooks(
        libraryId: String,
        pageSize: Int,
        pageNumber: Int,
    ): ApiResult<PagedItems<Book>> = dataRepository
        .fetchLibraryItems(
            libraryId = libraryId,
            pageSize = pageSize,
            pageNumber = pageNumber,
        )
        .map { libraryPageResponseToPagedBooks(it) }

    override suspend fun searchBooks(
        libraryId: String,
        query: String,
        limit: Int,
    ): ApiResult<List<Book>> = coroutineScope {
        val byTitle = async {
            dataRepository
                .searchBooks(libraryId, query, limit)
                .map { it.book }
                .map { it.map { response -> response.libraryItem } }
                .map { libraryItemListToBookList(it) }
        }

        val byAuthor = async {
            val searchResult = dataRepository.searchBooks(libraryId, query, limit)

            searchResult
                .map { it.authors }
                .map { authors -> authors.map { it.id } }
                .map { ids -> ids.map { id -> async { dataRepository.fetchAuthorItems(id) } } }
                .map { it.awaitAll() }
                .map { result ->
                    result
                        .flatMap { authorResponse ->
                            authorResponse
                                .fold(
                                    onSuccess = { it.libraryItems },
                                    onFailure = { emptyList() },
                                )
                        }
                }
                .map { libraryItemListToBookList(it) }
        }

        byTitle.await().flatMap { title -> byAuthor.await().map { author -> title + author } }
    }

    override suspend fun startPlayback(
        bookId: String,
        episodeId: String,
        supportedMimeTypes: List<String>,
        deviceId: String,
    ): ApiResult<PlaybackSession> {
        val request = PlaybackStartRequest(
            supportedMimeTypes = supportedMimeTypes,
            deviceInfo = DeviceInfo(
                clientName = getClientName(),
                deviceId = deviceId,
                deviceName = getClientName(),
            ),
            forceTranscode = false,
            forceDirectPlay = false,
            mediaPlayer = getClientName(),
        )

        return dataRepository
            .startPlayback(
                itemId = bookId,
                request = request,
            )
            .map { playbackSessionResponseToPlaybackSession(it) }
    }

    override suspend fun fetchBook(bookId: String): ApiResult<DetailedItem> = coroutineScope {
        val book = async { dataRepository.fetchBook(bookId) }
        val bookProgress = async { dataRepository.fetchLibraryItemProgress(bookId) }

        book.await().foldAsync(
            onSuccess = { item ->
                bookProgress
                    .await()
                    .fold(
                        onSuccess = { Success(bookResponseAndMediaProgressResponseToDetailedItem(item, it)) },
                        onFailure = { Success(bookResponseAndMediaProgressResponseToDetailedItem(item, null)) },
                    )
            },
            onFailure = { ApiResult.Error(it.code) },
        )
    }
}
