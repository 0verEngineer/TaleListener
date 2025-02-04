package org.overengineer.talelistener.content.cache.api


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import me.sujanpoudel.utils.paths.appCacheDirectory
import me.sujanpoudel.utils.paths.appDataDirectory
import okio.Path
import okio.Path.Companion.toPath
import org.overengineer.talelistener.content.cache.converter.bookEntityToBook
import org.overengineer.talelistener.content.cache.converter.bookEntityToDetailedItem
import org.overengineer.talelistener.content.cache.converter.bookEntityToRecentBook
import org.overengineer.talelistener.db.DBHolder
import org.overengineer.talelistener.domain.Book
import org.overengineer.talelistener.domain.DetailedItem
import org.overengineer.talelistener.domain.PlaybackProgress
import org.overengineer.talelistener.domain.RecentBook
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences
import org.overengineer.talelistener.sqldelight.BookChapterEntity
import org.overengineer.talelistener.sqldelight.BookEntity
import org.overengineer.talelistener.sqldelight.BookFileEntity
import org.overengineer.talelistener.sqldelight.MediaProgressEntity

class CachedBookRepository(
    private val settings: TaleListenerSharedPreferences,
    private val dbHolder: DBHolder
) {

    // Todo: Static value set on build from gradle
    private val packageName = "org.overengineer.talelistener"

    fun provideFilePath(bookId: String, fileId: String): Path {
        val path = appDataDirectory(packageName, true).toString() + "/$bookId/$fileId"
        return path.toPath()
    }

    fun provideBookCoverPath(bookId: String): Path {
        val path = appCacheDirectory(packageName, true).toString() + "/$bookId/cover.img"
        return path.toPath()
    }

    suspend fun removeBook(bookId: String) {
        withContext(Dispatchers.IO) {
            dbHolder.bookQueries.deleteBook(bookId)
        }
    }

    suspend fun cacheBook(book: DetailedItem) {
        withContext(Dispatchers.IO) {
            val q = dbHolder.bookQueries

            val bookEntity = BookEntity(
                id = book.id,
                title = book.title,
                author = book.author,
                duration = book.chapters.sumOf { it.duration }.toLong(),
                libraryId = book.libraryId
            )

            val bookFiles = book
                .files
                .map { file ->
                    BookFileEntity(
                        id = file.id,
                        name = file.name,
                        duration = file.duration,
                        mimeType = file.mimeType,
                        bookId = book.id,
                    )
                }

            val bookChapters = book
                .chapters
                .map { chapter ->
                    BookChapterEntity(
                        id = chapter.id,
                        duration = chapter.duration,
                        start = chapter.start,
                        end = chapter.end,
                        title = chapter.title,
                        bookId = book.id,
                    )
                }

            val mediaProgress = book
                .progress
                ?.let { progress ->
                    MediaProgressEntity(
                        bookId = book.id,
                        currentTime = progress.currentTime,
                        isFinished = progress.isFinished,
                        lastUpdate = progress.lastUpdate,
                    )
                }

            q.transaction {
                q.upsertBook(bookEntity)

                bookFiles.forEach {
                    q.upsertBookFile(it)
                }

                bookChapters.forEach {
                    q.upsertBookChapter(it)
                }

                mediaProgress?.let { q.upsertMediaProgress(it) }
            }
        }
    }

    suspend fun fetchCachedBookIds(): List<String> = withContext(Dispatchers.IO) {
            dbHolder.bookQueries.fetchBookIds(settings.getPreferredLibrary()?.id).executeAsList()
    }

    suspend fun fetchBooks(
        pageNumber: Int,
        pageSize: Int
    ): List<Book> = withContext(Dispatchers.IO) {
            dbHolder.bookQueries.fetchCachedBooks(
                libraryId = settings.getPreferredLibrary()?.id,
                value_ = pageSize.toLong(),
                value__ = pageSize.toLong(),
                value___ = pageNumber.toLong()
            ).executeAsList()
                .map { bookEntityToBook(it) }
    }

    suspend fun searchBooks(
        query: String
    ): List<Book> = withContext(Dispatchers.IO) {
        dbHolder.bookQueries.searchCachedBooks(
            libraryId = settings.getPreferredLibrary()?.id,
            value_ = query,
            value__ = query
        ).executeAsList()
            .map { bookEntityToBook(it) }
    }

    suspend fun fetchRecentBooks(): List<RecentBook> = withContext(Dispatchers.IO) {
        val q = dbHolder.bookQueries

        val recentBooks = q.fetchRecentlyListenedCachedBooks(
            libraryId = settings.getPreferredLibrary()?.id
        ).executeAsList()

        val progress = recentBooks
            .map { it.id }
            .map { q.fetchMediaProgress(it).executeAsOne() }
            .associate { it.bookId to it.currentTime }

        recentBooks.map { bookEntityToRecentBook(it, progress[it.id]) }
    }

    suspend fun fetchBook(
        bookId: String
    ): DetailedItem? = withContext(Dispatchers.IO) {
        val q = dbHolder.bookQueries

        val book = q.fetchBook(bookId).executeAsOneOrNull() ?: return@withContext null
        val files = q.fetchBookFilesForBookId(bookId).executeAsList()
        val chapters = q.fetchBookChaptersForBookId(bookId).executeAsList()
        val progress = q.fetchMediaProgress(bookId).executeAsOneOrNull()

        bookEntityToDetailedItem(book, files, chapters, progress)
    }

    suspend fun syncProgress(
        bookId: String,
        progress: PlaybackProgress
    ) = withContext(Dispatchers.IO) {
        val chapters = dbHolder.bookQueries.fetchBookChaptersForBookId(bookId).executeAsList()

        val entity = MediaProgressEntity(
            bookId = bookId,
            currentTime = progress.currentTime,
            isFinished = progress.currentTime == chapters.sumOf { it.duration },
            lastUpdate = Clock.System.now().toEpochMilliseconds(),
        )

        // todo: cacheBook is not called yet so we cannot save the progress
        //dbHolder.bookQueries.upsertMediaProgress(entity)
    }
}