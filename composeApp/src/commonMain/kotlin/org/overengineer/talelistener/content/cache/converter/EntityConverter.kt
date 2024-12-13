package org.overengineer.talelistener.content.cache.converter

import org.overengineer.talelistener.domain.Book
import org.overengineer.talelistener.domain.BookCachedState
import org.overengineer.talelistener.domain.BookChapter
import org.overengineer.talelistener.domain.BookFile
import org.overengineer.talelistener.domain.DetailedItem
import org.overengineer.talelistener.domain.MediaProgress
import org.overengineer.talelistener.domain.RecentBook
import org.overengineer.talelistener.sqldelight.BookChapterEntity
import org.overengineer.talelistener.sqldelight.BookEntity
import org.overengineer.talelistener.sqldelight.BookFileEntity
import org.overengineer.talelistener.sqldelight.MediaProgressEntity


fun bookEntityToBook(book: BookEntity): Book {
    return Book(
        id = book.id,
        title = book.title,
        author = book.author,
        duration = book.duration.toInt(),
        cachedState = BookCachedState.CACHED
    )
}

fun bookEntityToRecentBook(book: BookEntity, currentTime: Double?): RecentBook {
    return RecentBook(
        id = book.id,
        title = book.title,
        author = book.author,
        listenedPercentage = currentTime
            ?.let { it / book.duration }
            ?.let { it * 100 }
            ?.toInt(),    )
}

fun bookEntityToDetailedItem(
    book: BookEntity,
    files: List<BookFileEntity>,
    chapters: List<BookChapterEntity>,
    progress: MediaProgressEntity?
): DetailedItem {
    return DetailedItem(
        id = book.id,
        title = book.title,
        author = book.author,
        libraryId = book.libraryId,
        files = files.map { entity ->
            BookFile(
                id = entity.id,
                name = entity.name,
                duration = entity.duration,
                mimeType = entity.mimeType
            )
        },
        chapters = chapters.map { entity ->
            BookChapter(
                duration = entity.duration,
                start = entity.start,
                end = entity.end,
                title = entity.title,
                id = entity.id
            )
        },
        progress = progress?.let { entity ->
            MediaProgress(
                currentTime = entity.currentTime,
                isFinished = entity.isFinished,
                lastUpdate = entity.lastUpdate
            )
        }
    )
}