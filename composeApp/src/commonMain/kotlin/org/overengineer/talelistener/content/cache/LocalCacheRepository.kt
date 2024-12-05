
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.domain
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Removed javax singleton stuff
 * - Replaced android specific stuff with kotlin multiplatform stuff
 */

package org.overengineer.talelistener.content.cache

import io.ktor.http.Url
import io.ktor.utils.io.ByteReadChannel
import org.overengineer.talelistener.channel.common.ApiError
import org.overengineer.talelistener.channel.common.ApiResult
//import org.overengineer.talelistener.content.cache.api.CachedBookRepository
//import org.overengineer.talelistener.content.cache.api.CachedLibraryRepository
import org.overengineer.talelistener.domain.Book
import org.overengineer.talelistener.domain.Library
import org.overengineer.talelistener.domain.PagedItems
import org.overengineer.talelistener.domain.PlaybackProgress
import org.overengineer.talelistener.domain.PlaybackSession
import org.overengineer.talelistener.domain.RecentBook

// todo impl
class LocalCacheRepository constructor(
    //private val cachedBookRepository: CachedBookRepository,
    //private val cachedLibraryRepository: CachedLibraryRepository,
    //private val properties: CacheBookStorageProperties,
) {

    fun provideFileUri(libraryItemId: String, fileId: String): Url? =
        throw NotImplementedError("provideFileUri not implemented")
        /*cachedBookRepository
            .provideFileUri(libraryItemId, fileId)
            .takeIf { it.toFile().exists() }*/

    /**
     * For the local cache we avoiding to create intermediary entity like Session and using BookId
     * as a Playback Session Key
     */
    suspend fun syncProgress(
        bookId: String,
        progress: PlaybackProgress,
    ): ApiResult<Unit> {
        throw NotImplementedError("syncProgress not implemented")
        //cachedBookRepository.syncProgress(bookId, progress)
        //return ApiResult.Success(Unit)
    }

    fun fetchBookCover(bookId: String): ApiResult<ByteReadChannel> {
        throw NotImplementedError("fetchBookCover not implemented")
        /*val cover = cachedBookRepository
            .provideBookCover(bookId)

        return when (cover.exists()) {
            true -> ApiResult.Success(cover.inputStream())
            false -> ApiResult.Error(ApiError.InternalError)
        }*/
    }

    suspend fun searchBooks(
        query: String,
    ): ApiResult<List<Book>> = throw NotImplementedError("searchBooks not imlemented") /*cachedBookRepository
        .searchBooks(query = query)
        .filter { checkBookIntegrity(it.id) }
        .let { ApiResult.Success(it) }*/

    suspend fun fetchBooks(
        pageSize: Int,
        pageNumber: Int,
    ): ApiResult<PagedItems<Book>> {
        throw NotImplementedError("fetchBooks not implemented")
        /*val books = cachedBookRepository
            .fetchBooks(pageNumber = pageNumber, pageSize = pageSize)
            .filter { checkBookIntegrity(it.id) }

        return ApiResult
            .Success(
                PagedItems(
                    items = books,
                    currentPage = pageNumber,
                ),
            )*/
    }

    suspend fun fetchLibraries(): ApiResult<List<Library>> = throw NotImplementedError("fetchLibraries not implemented") /*cachedLibraryRepository
        .fetchLibraries()
        .let { ApiResult.Success(it) }*/

    suspend fun updateLibraries(libraries: List<Library>) {
        throw NotImplementedError("updateLibraries not implemented")
        //cachedLibraryRepository.cacheLibraries(libraries)
    }

    /**
     * For the local cache we avoiding to create intermediary entity like Session and using BookId
     * as a Playback Session Key
     */
    fun startPlayback(
        bookId: String,
    ): ApiResult<PlaybackSession> =
        throw NotImplementedError("startPlayback not implemented")
        /*ApiResult
            .Success(
                PlaybackSession(
                    bookId = bookId,
                    sessionId = bookId,
                ),
            )*/

    suspend fun fetchRecentListenedBooks(): ApiResult<List<RecentBook>> =
        throw NotImplementedError("fetchRecentListenedBooks not implemented")
        /*cachedBookRepository
            .fetchRecentBooks()
            .filter { checkBookIntegrity(it.id) }
            .let { ApiResult.Success(it) }*/

    suspend fun fetchBook(bookId: String): Nothing = throw NotImplementedError("fetchBook not implemented") /*cachedBookRepository
        .fetchBook(bookId)
        ?.takeIf { checkBookIntegrity(bookId) }*/

    suspend fun fetchCachedBookIds(): Nothing = throw NotImplementedError("fetchCachedBookIds, not implemented") /*cachedBookRepository
        .fetchCachedBooksIds()
        .filter { checkBookIntegrity(it) }*/

    private suspend fun checkBookIntegrity(bookId: String): Boolean {
        throw NotImplementedError("checkBookIntegrity not implemented")
        /*val book = cachedBookRepository.fetchBook(bookId) ?: return false
        return book.files.all { properties.provideMediaCachePatch(bookId, it.id).exists() }*/
    }
}
