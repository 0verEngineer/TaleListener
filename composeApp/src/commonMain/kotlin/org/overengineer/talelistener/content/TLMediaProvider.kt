/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.domain
 * Original file name: LissenMediaProvider
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Removed javax singleton stuff
 * - Changes for kotlin multiplatform
 * - Moved channels from constructor into class and added AudiobookshelfChannelProvider to constructor
 * - Migrated logging to Napier
 * - Added provideFilePath for local paths
 * - Refactored the methods to use return if (isOffline) instead of the old forceCache logic
 * - Added isTokenValid method
 */

package org.overengineer.talelistener.content

import io.github.aakira.napier.Napier
import io.ktor.http.Url
import okio.BufferedSource
import okio.Path
import org.overengineer.talelistener.channel.audiobookshelf.AudiobookshelfChannelProvider
import org.overengineer.talelistener.channel.common.ApiError
import org.overengineer.talelistener.channel.common.ApiResult
import org.overengineer.talelistener.channel.common.ChannelAuthService
import org.overengineer.talelistener.channel.common.ChannelCode
import org.overengineer.talelistener.channel.common.ChannelProvider
import org.overengineer.talelistener.channel.common.MediaChannel
import org.overengineer.talelistener.content.cache.LocalCacheRepository
import org.overengineer.talelistener.domain.Book
import org.overengineer.talelistener.domain.BookCachedState.CACHED
import org.overengineer.talelistener.domain.DetailedItem
import org.overengineer.talelistener.domain.Library
import org.overengineer.talelistener.domain.PagedItems
import org.overengineer.talelistener.domain.PlaybackProgress
import org.overengineer.talelistener.domain.PlaybackSession
import org.overengineer.talelistener.domain.RecentBook
import org.overengineer.talelistener.domain.UserAccount
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences

class TLMediaProvider (
    private val preferences: TaleListenerSharedPreferences,
    private val audiobookshelfChannelProvider: AudiobookshelfChannelProvider,
    private val localCacheRepository: LocalCacheRepository,
) {

    private val channels: Map<ChannelCode, ChannelProvider> = mapOf(pair = Pair(ChannelCode.AUDIOBOOKSHELF, audiobookshelfChannelProvider))

    fun provideFilePath(
        libraryItemId: String,
        chapterId: String,
    ): Path? = localCacheRepository.provideFilePath(libraryItemId, chapterId)

    fun provideFileUrl(
        libraryItemId: String,
        chapterId: String,
    ): Url = providePreferredChannel().provideFileUrl(libraryItemId, chapterId)

    suspend fun syncProgress(
        sessionId: String,
        bookId: String,
        progress: PlaybackProgress,
    ): ApiResult<Unit> {
        Napier.d("Syncing Progress for $bookId, $progress")

        return if (!preferences.isConnectedAndOnline()) {
            localCacheRepository.syncProgress(bookId, progress)
        } else {
            providePreferredChannel()
                .syncProgress(sessionId, progress)
                .also { localCacheRepository.syncProgress(bookId, progress) }
        }
    }

    suspend fun fetchBookCover(
        bookId: String
    ): ApiResult<BufferedSource> {
        val isConnected = preferences.isConnectedAndOnline()
        Napier.d("Fetching Cover stream for $bookId, isConnected: $isConnected")

        return if (!isConnected) {
            localCacheRepository.fetchBookCover(bookId)
        } else {
            providePreferredChannel().fetchBookCover(bookId)
        }
    }

    suspend fun searchBooks(
        libraryId: String,
        query: String,
        limit: Int,
    ): ApiResult<List<Book>> {
        val isConnected = preferences.isConnectedAndOnline()
        Napier.d("Searching books with query $query of library: $libraryId isConnected: $isConnected")

        return if (!isConnected) {
            localCacheRepository.searchBooks(query)
        } else {
            providePreferredChannel().searchBooks(
                libraryId = libraryId,
                query = query,
                limit = limit
            )
        }
    }

    suspend fun fetchBooks(
        libraryId: String,
        pageSize: Int,
        pageNumber: Int,
    ): ApiResult<PagedItems<Book>> {
        val isConnected = preferences.isConnectedAndOnline()
        Napier.d("Fetching page $pageNumber of library: $libraryId isConnected: $isConnected")

        return if (!isConnected) {
            localCacheRepository.fetchBooks(pageSize, pageNumber)
        } else {
            providePreferredChannel()
                .fetchBooks(libraryId, pageSize, pageNumber)
                .map { flagCached(it) }
        }
    }

    suspend fun fetchLibraries(): ApiResult<List<Library>> {
        val isConnected = preferences.isConnectedAndOnline()
        Napier.d("Fetching List of libraries, isConnected: $isConnected")

        return if (!isConnected) {
            localCacheRepository.fetchLibraries()
        } else {
            providePreferredChannel()
                .fetchLibraries()
                .also {
                    it.foldAsync(
                        onSuccess = { libraries -> localCacheRepository.updateLibraries(libraries) },
                        onFailure = {},
                    )
                }
        }
    }

    suspend fun startPlayback(
        bookId: String,
        chapterId: String,
        supportedMimeTypes: List<String>,
        deviceId: String,
    ): ApiResult<PlaybackSession> {
        val isConnected = preferences.isConnectedAndOnline()
        Napier.d("Starting Playback for $bookId, $supportedMimeTypes are supported, isConnected: $isConnected")

        return if (!isConnected) {
            localCacheRepository.startPlayback(bookId)
        } else {
            providePreferredChannel().startPlayback(
                bookId = bookId,
                episodeId = chapterId,
                supportedMimeTypes = supportedMimeTypes,
                deviceId = deviceId,
            )
        }
    }

    suspend fun fetchRecentListenedBooks(
        libraryId: String,
    ): ApiResult<List<RecentBook>> {
        val isConnected = preferences.isConnectedAndOnline()
        Napier.d("Fetching Recent books of library $libraryId, isConnected: $isConnected")

        return if (!isConnected) {
            localCacheRepository.fetchRecentListenedBooks()
        } else {
            providePreferredChannel().fetchRecentListenedBooks(libraryId)
        }
    }

    suspend fun fetchBook(
        bookId: String,
    ): ApiResult<DetailedItem> {
        val isConnected = preferences.isConnectedAndOnline()
        Napier.d("Fetching Detailed book info for $bookId, isConnected: $isConnected")

        return if (!isConnected) {
            localCacheRepository
                .fetchBook(bookId)
                ?.let { ApiResult.Success(it) }
                ?: ApiResult.Error(ApiError.InternalError)
        } else {
            providePreferredChannel()
                .fetchBook(bookId)
                .map { syncFromLocalProgress(it) }
        }
    }

    suspend fun authorize(
        host: String,
        username: String,
        password: String,
    ): ApiResult<UserAccount> {
        Napier.d("Authorizing for $username@$host")
        return provideAuthService().authorize(host, username, password)
    }

    private suspend fun syncFromLocalProgress(detailedItem: DetailedItem): DetailedItem {
        val cachedBook = localCacheRepository.fetchBook(detailedItem.id) ?: return detailedItem

        val cachedProgress = cachedBook.progress ?: return detailedItem
        val channelProgress = detailedItem.progress

        val updatedProgress = listOfNotNull(cachedProgress, channelProgress)
            .maxByOrNull { it.lastUpdate }
            ?: return detailedItem

        Napier.d("""
            Merging local playback progress into channel-fetched:
                            Channel Progress: $channelProgress
                            Local Progress: $cachedProgress
                            Final Progress: $updatedProgress
        """.trimIndent())

        return detailedItem.copy(progress = updatedProgress)
    }

    suspend fun fetchConnectionInfo() = providePreferredChannel().fetchConnectionInfo()

    suspend fun checkTokenValidAndSetIsServerConnected(): Boolean {
        val response = providePreferredChannel().fetchConnectionInfo()
        return response.fold(
            onSuccess = {
                Napier.d("checkTokenValidAndSetIsServerConnected, isServerConnected: true")
                preferences.setIsServerConnected(true)
                return@fold true
            },
            onFailure = {
                if (it.code == ApiError.Unauthorized) {
                    return@fold false
                }
                Napier.d("checkTokenValidAndSetIsServerConnected, isServerConnected: false")
                preferences.setIsServerConnected(false)
                return@fold false
            }
        )
    }

    private suspend fun flagCached(page: PagedItems<Book>): PagedItems<Book> {
        val cachedBooks = localCacheRepository.fetchCachedBookIds()

        val items = page
            .items
            .map { book ->
                when (cachedBooks.contains(book.id)) {
                    true ->
                        book
                            .copy(cachedState = CACHED)
                            .also { Napier.d("${book.id} flagged as Cached") }

                    false -> book
                }
            }

        return page.copy(items = items)
    }

    fun provideAuthService(): ChannelAuthService = channels[preferences.getChannel()]
        ?.provideChannelAuth()
        ?: throw IllegalStateException("Selected auth service has been requested but not selected")

    fun providePreferredChannel(): MediaChannel = channels[preferences.getChannel()]
        ?.provideMediaChannel()
        ?: throw IllegalStateException("Selected auth service has been requested but not selected")

}
