package org.overengineer.talelistener.content.cache


import kotlinx.datetime.Clock
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

    // todo impl provideFileUri

    // todo impl provideBookCover

    fun removeBook(bookId: String) {
        dbHolder.bookQueries.deleteBook(bookId)
    }

    fun cacheBook(book: DetailedItem) {
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

    fun fetchCachedBookIds() = dbHolder.bookQueries.fetchBookIds(settings.getPreferredLibrary()?.id)

    fun fetchBooks(
        pageNumber: Int,
        pageSize: Int
    ): List<Book> {
        return dbHolder.bookQueries.fetchCachedBooks(
            libraryId = settings.getPreferredLibrary()?.id,
            value_ = pageSize.toLong(),
            value__ = pageSize.toLong(),
            value___ = pageNumber.toLong()
        ).executeAsList().map {
            bookEntityToBook(it)
        }
    }

    fun searchBooks(
        query: String
    ): List<BookEntity> {
        return dbHolder.bookQueries.searchCachedBooks(
            libraryId = settings.getPreferredLibrary()?.id,
            value_ = query,
            value__ = query
        ).executeAsList()
    }

    fun fetchRecentBooks(): List<RecentBook> {
        val q = dbHolder.bookQueries

        val recentBooks = q.fetchRecentlyListenedCachedBooks(
            libraryId = settings.getPreferredLibrary()?.id
        ).executeAsList()

        val progress = recentBooks
            .map { it.id }
            .map { q.fetchMediaProgress(it).executeAsOne() }
            .associate { it.bookId to it.currentTime }

        return recentBooks.map { bookEntityToRecentBook(it, progress[it.id]) }
    }

    fun fetchBook(
        bookId: String
    ): DetailedItem? {
        val q = dbHolder.bookQueries

        val book = q.fetchBook(bookId).executeAsOneOrNull() ?: return null
        val files = q.fetchBookFilesForBookId(bookId).executeAsList()
        val chapters = q.fetchBookChaptersForBookId(bookId).executeAsList()
        val progress = q.fetchMediaProgress(bookId).executeAsOneOrNull()

        return bookEntityToDetailedItem(book, files, chapters, progress)
    }

    fun syncProgress(
        bookId: String,
        progress: PlaybackProgress
    ) {
        val book = dbHolder.bookQueries.fetchBook(bookId).executeAsOneOrNull()
        val chapters = dbHolder.bookQueries.fetchBookChaptersForBookId(bookId).executeAsList()

        val entity = MediaProgressEntity(
            bookId = bookId,
            currentTime = progress.currentTime,
            isFinished = progress.currentTime == chapters.sumOf { it.duration },
            lastUpdate = Clock.System.now().toEpochMilliseconds(),
        )
    }
}