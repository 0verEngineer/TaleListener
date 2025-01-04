
package org.overengineer.talelistener.content.cache

import okio.BufferedSource
import okio.FileSystem
import okio.Path
import okio.SYSTEM
import okio.buffer
import org.overengineer.talelistener.channel.common.ApiError
import org.overengineer.talelistener.channel.common.ApiResult
import org.overengineer.talelistener.content.cache.api.CachedBookRepository
import org.overengineer.talelistener.content.cache.api.CachedLibraryRepository
import org.overengineer.talelistener.domain.Book
import org.overengineer.talelistener.domain.Library
import org.overengineer.talelistener.domain.PagedItems
import org.overengineer.talelistener.domain.PlaybackProgress
import org.overengineer.talelistener.domain.PlaybackSession
import org.overengineer.talelistener.domain.RecentBook

class LocalCacheRepository (
    private val cachedBookRepository: CachedBookRepository,
    private val cachedLibraryRepository: CachedLibraryRepository
) {
    private val fileSystem = FileSystem.SYSTEM

    fun provideFilePath(libraryItemId: String, fileId: String): Path? {
        return cachedBookRepository
            .provideFilePath(libraryItemId, fileId)
            .takeIf { fileSystem.exists(it) }
    }

    suspend fun syncProgress(
        bookId: String,
        progress: PlaybackProgress,
    ): ApiResult<Unit> {
        cachedBookRepository.syncProgress(bookId, progress)
        return ApiResult.Success(Unit)
    }

    fun fetchBookCover(bookId: String): ApiResult<BufferedSource> {
        val cover = cachedBookRepository
            .provideBookCoverPath(bookId)

        return when (fileSystem.exists(cover)) {
            true -> ApiResult.Success( fileSystem.openReadOnly(cover).source().buffer())
            false -> ApiResult.Error(ApiError.InternalError)
        }
    }

    suspend fun searchBooks(
        query: String,
    ): ApiResult<List<Book>> = cachedBookRepository
        .searchBooks(query = query)
        .filter { checkBookIntegrity(it.id) }
        .let { ApiResult.Success(it) }

    suspend fun fetchBooks(
        pageSize: Int,
        pageNumber: Int,
    ): ApiResult<PagedItems<Book>> {
        val books = cachedBookRepository
            .fetchBooks(pageNumber = pageNumber, pageSize = pageSize)
            .filter { checkBookIntegrity(it.id) }

        return ApiResult
            .Success(
                PagedItems(
                    items = books,
                    currentPage = pageNumber,
                ),
            )
    }

    suspend fun fetchLibraries(): ApiResult<List<Library>> = cachedLibraryRepository
        .fetchLibraries()
        .let { ApiResult.Success(it) }

    suspend fun updateLibraries(libraries: List<Library>) {
        cachedLibraryRepository.cacheLibraries(libraries)
    }

    /**
     * For the local cache we avoiding to create intermediary entity like Session and using BookId
     * as a Playback Session Key
     */
    fun startPlayback(
        bookId: String,
    ): ApiResult<PlaybackSession> =
        ApiResult
            .Success(
                PlaybackSession(
                    bookId = bookId,
                    sessionId = bookId,
                ),
            )

    suspend fun fetchRecentListenedBooks(): ApiResult<List<RecentBook>> =
        cachedBookRepository
            .fetchRecentBooks()
            .filter { checkBookIntegrity(it.id) }
            .let { ApiResult.Success(it) }

    suspend fun fetchBook(bookId: String) = cachedBookRepository
        .fetchBook(bookId)
        ?.takeIf { checkBookIntegrity(bookId) }

    suspend fun fetchCachedBookIds() = cachedBookRepository
        .fetchCachedBookIds()
        .filter { checkBookIntegrity(it) }

    private suspend fun checkBookIntegrity(bookId: String): Boolean {
        val book = cachedBookRepository.fetchBook(bookId) ?: return false
        return book.files.all { fileSystem.exists(cachedBookRepository.provideFilePath(bookId, it.id)) }
    }
}
