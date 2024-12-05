
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.domain
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Removed javax singleton stuff
 * - Changes for kotlin multiplatform
 * - Moved channels from constructor into class and added AudiobookshelfChannelProvider to constructor
 * - Migrated logging to Napier
 */

package org.overengineer.talelistener.content

import io.github.aakira.napier.Napier
import io.ktor.http.Url
import io.ktor.utils.io.ByteReadChannel
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

class LissenMediaProvider (
    private val sharedPreferences: TaleListenerSharedPreferences,
    private val audiobookshelfChannelProvider: AudiobookshelfChannelProvider,
    private val localCacheRepository: LocalCacheRepository,
) {

    private val channels: Map<ChannelCode, ChannelProvider> = mapOf(pair = Pair(ChannelCode.AUDIOBOOKSHELF, audiobookshelfChannelProvider))

    fun provideFileUri(
        libraryItemId: String,
        chapterId: String,
    ): ApiResult<Url> {
        Napier.d("Fetching File $libraryItemId and $chapterId URI")

        return when (sharedPreferences.isForceCache()) {
            true ->
                localCacheRepository
                    .provideFileUri(libraryItemId, chapterId)
                    ?.let { ApiResult.Success(it) }
                    ?: ApiResult.Error(ApiError.InternalError)

            false ->
                localCacheRepository
                    .provideFileUri(libraryItemId, chapterId)
                    ?.let { ApiResult.Success(it) }
                    ?: providePreferredChannel()
                        .provideFileUri(libraryItemId, chapterId)
                        .let { ApiResult.Success(it) }
        }
    }

    suspend fun syncProgress(
        sessionId: String,
        bookId: String,
        progress: PlaybackProgress,
    ): ApiResult<Unit> {
        Napier.d("Syncing Progress for $bookId, $progress")

        return when (sharedPreferences.isForceCache()) {
            true -> localCacheRepository.syncProgress(bookId, progress)
            false -> providePreferredChannel()
                .syncProgress(sessionId, progress)
                .also { localCacheRepository.syncProgress(bookId, progress) }
        }
    }

    suspend fun fetchBookCover(
        bookId: String,
    ): ApiResult<ByteReadChannel> {
        Napier.d("Fetching Cover stream for $bookId")

        return when (sharedPreferences.isForceCache()) {
            true -> localCacheRepository.fetchBookCover(bookId)
            false -> providePreferredChannel().fetchBookCover(bookId)
        }
    }

    suspend fun searchBooks(
        libraryId: String,
        query: String,
        limit: Int,
    ): ApiResult<List<Book>> {
        Napier.d("Searching books with query $query of library: $libraryId")

        return when (sharedPreferences.isForceCache()) {
            true -> localCacheRepository.searchBooks(query)
            false -> providePreferredChannel()
                .searchBooks(
                    libraryId = libraryId,
                    query = query,
                    limit = limit,
                )
        }
    }

    suspend fun fetchBooks(
        libraryId: String,
        pageSize: Int,
        pageNumber: Int,
    ): ApiResult<PagedItems<Book>> {
        Napier.d("Fetching page $pageNumber of library: $libraryId")

        return when (sharedPreferences.isForceCache()) {
            true -> localCacheRepository.fetchBooks(pageSize, pageNumber)
            false -> {
                providePreferredChannel()
                    .fetchBooks(libraryId, pageSize, pageNumber)
                    .map { flagCached(it) }
            }
        }
    }

    suspend fun fetchLibraries(): ApiResult<List<Library>> {
        Napier.d("Fetching List of libraries")

        return when (sharedPreferences.isForceCache()) {
            true -> localCacheRepository.fetchLibraries()
            false -> providePreferredChannel()
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
        Napier.d("Starting Playback for $bookId. $supportedMimeTypes are supported")

        return when (sharedPreferences.isForceCache()) {
            true -> localCacheRepository.startPlayback(bookId)
            false -> providePreferredChannel().startPlayback(
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
        Napier.d("Fetching Recent books of library $libraryId")

        return when (sharedPreferences.isForceCache()) {
            true -> localCacheRepository.fetchRecentListenedBooks()
            false -> providePreferredChannel().fetchRecentListenedBooks(libraryId)
        }
    }

    suspend fun fetchBook(
        bookId: String,
    ): ApiResult<DetailedItem> {
        Napier.d("Fetching Detailed book info for $bookId")

        return when (sharedPreferences.isForceCache()) {
            true ->
                localCacheRepository
                    .fetchBook(bookId)
                    ?.let { ApiResult.Success(it) }
                    ?: ApiResult.Error(ApiError.InternalError)

            false -> providePreferredChannel()
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

    // todo impl
    private suspend fun syncFromLocalProgress(detailedItem: DetailedItem): DetailedItem {
        throw NotImplementedError("syncFromLocalProgress not implemented")
        /*val cachedBook = localCacheRepository.fetchBook(detailedItem.id) ?: return detailedItem

        val cachedProgress = cachedBook.progress ?: return detailedItem
        val channelProgress = detailedItem.progress

        val updatedProgress = listOfNotNull(cachedProgress, channelProgress)
            .maxByOrNull { it.lastUpdate }
            ?: return detailedItem*/

        /*Napier.d("""
            Merging local playback progress into channel-fetched:
                            Channel Progress: $channelProgress
                            Local Progress: $cachedProgress
                            Final Progress: $updatedProgress
        """.trimIndent())

        return detailedItem.copy(progress = updatedProgress)*/
    }

    suspend fun fetchConnectionInfo() = providePreferredChannel().fetchConnectionInfo()

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

    fun provideAuthService(): ChannelAuthService = channels[sharedPreferences.getChannel()]
        ?.provideChannelAuth()
        ?: throw IllegalStateException("Selected auth service has been requested but not selected")

    fun providePreferredChannel(): MediaChannel = channels[sharedPreferences.getChannel()]
        ?.provideMediaChannel()
        ?: throw IllegalStateException("Selected auth service has been requested but not selected")

}
