
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * This file includes code of all converters in:
 *   - org.grakovne.lissen.channel.audiobookshelf.converter
 *   - org.grakovne.lissen.channel.audiobookshelf.library.converter
 */


package org.overengineer.talelistener.content.cache.converter

import org.overengineer.talelistener.channel.audiobookshelf.common.model.MediaProgressResponse
import org.overengineer.talelistener.channel.audiobookshelf.common.model.connection.ConnectionInfoResponse
import org.overengineer.talelistener.channel.audiobookshelf.common.model.metadata.LibraryResponse
import org.overengineer.talelistener.channel.audiobookshelf.common.model.playback.PlaybackSessionResponse
import org.overengineer.talelistener.channel.audiobookshelf.common.model.user.LoggedUserResponse
import org.overengineer.talelistener.channel.audiobookshelf.common.model.user.PersonalizedFeedResponse
import org.overengineer.talelistener.channel.audiobookshelf.library.model.BookResponse
import org.overengineer.talelistener.channel.audiobookshelf.library.model.LibraryAuthorResponse
import org.overengineer.talelistener.channel.audiobookshelf.library.model.LibraryItem
import org.overengineer.talelistener.channel.audiobookshelf.library.model.LibraryItemsResponse
import org.overengineer.talelistener.channel.common.ConnectionInfo
import org.overengineer.talelistener.channel.common.LibraryType
import org.overengineer.talelistener.domain.Book
import org.overengineer.talelistener.domain.BookCachedState
import org.overengineer.talelistener.domain.BookChapter
import org.overengineer.talelistener.domain.BookFile
import org.overengineer.talelistener.domain.DetailedItem
import org.overengineer.talelistener.domain.Library
import org.overengineer.talelistener.domain.MediaProgress
import org.overengineer.talelistener.domain.PagedItems
import org.overengineer.talelistener.domain.PlaybackSession
import org.overengineer.talelistener.domain.RecentBook
import org.overengineer.talelistener.domain.UserAccount
import org.overengineer.talelistener.sqldelight.BookChapterEntity
import org.overengineer.talelistener.sqldelight.BookEntity
import org.overengineer.talelistener.sqldelight.BookFileEntity
import org.overengineer.talelistener.sqldelight.LibraryEntity
import org.overengineer.talelistener.sqldelight.MediaProgressEntity


private const val LABEL_CONTINUE_LISTENING = "LabelContinueListening"

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

fun libraryEntityToLibrary(library: LibraryEntity): Library {
    return Library(
        library.id,
        library.title,
        library.type
    )
}

fun libraryPageResponseToPagedBooks(libraries: LibraryItemsResponse) : PagedItems<Book> {
    return libraries.results
        .mapNotNull {
            val title = it.media.metadata.title ?: return@mapNotNull null

            Book(
                id = it.id,
                title = title,
                author = it.media.metadata.authorName,
                cachedState = BookCachedState.ABLE_TO_CACHE,
                duration = it.media.duration.toInt(),
            )
        }
        .let {
            PagedItems(
                items = it,
                currentPage = libraries.page,
            )
        }
}

fun connectionInfoResponseToConnectionInfo(info: ConnectionInfoResponse): ConnectionInfo {
    return ConnectionInfo(
        username = info.user.username,
        serverVersion = info.serverSettings?.version,
        buildNumber = info.serverSettings?.buildNumber
    )
}

fun libraryResponseToLibraryList(response: LibraryResponse): List<Library> {
    return response.libraries
        .map {
            it
                .mediaType
                .toLibraryType()
                .let { type -> Library(it.id, it.name, type) }
        }
}

fun loggedUserResponseToUserAccount(response: LoggedUserResponse): UserAccount {
    return UserAccount(
        token = response.user.token,
        preferredLibraryId = response.userDefaultLibraryId
    )
}

fun playbackSessionResponseToPlaybackSession(response: PlaybackSessionResponse): PlaybackSession {
    return PlaybackSession(
        sessionId = response.id,
        bookId = response.libraryItemId
    )
}

fun personalizedFeedResponseAndProgressToRecentBookList(
    response: List<PersonalizedFeedResponse>,
    progress: Map<String, Double>
): List<RecentBook> {
    return response
        .find { it.labelStringKey == LABEL_CONTINUE_LISTENING }
        ?.entities
        ?.distinctBy { it.id }
        ?.map {
            RecentBook(
                id = it.id,
                title = it.media.metadata.title,
                author = it.media.metadata.authorName,
                listenedPercentage = progress[it.id]?.let { it * 100 }?.toInt(),
            )
        } ?: emptyList()
}

fun bookResponseAndMediaProgressResponseToDetailedItem(
    item: BookResponse,
    progressResponse: MediaProgressResponse? = null
): DetailedItem {
    val maybeChapters = item
        .media
        .chapters
        ?.takeIf { it.isNotEmpty() }
        ?.map {
            BookChapter(
                start = it.start,
                end = it.end,
                title = it.title,
                id = it.id,
                duration = it.end - it.start,
            )
        }

    val filesAsChapters: () -> List<BookChapter> = {
        item
            .media
            .audioFiles
            ?.sortedBy { it.index }
            ?.fold(0.0 to mutableListOf<BookChapter>()) { (accDuration, chapters), file ->
                chapters.add(
                    BookChapter(
                        start = accDuration,
                        end = accDuration + file.duration,
                        title = file.metaTags?.tagTitle
                            ?: file.metadata.filename.removeSuffix(file.metadata.ext),
                        duration = file.duration,
                        id = file.ino,
                    ),
                )
                accDuration + file.duration to chapters
            }
            ?.second
            ?: emptyList()
    }

    return DetailedItem(
        id = item.id,
        title = item.media.metadata.title,
        author = item.media.metadata.authors?.joinToString(", ", transform = LibraryAuthorResponse::name),
        files = item
            .media
            .audioFiles
            ?.sortedBy { it.index }
            ?.map {
                BookFile(
                    id = it.ino,
                    name = it.metaTags
                        ?.tagTitle
                        ?: (it.metadata.filename.removeSuffix(it.metadata.ext)),
                    duration = it.duration,
                    mimeType = it.mimeType,
                )
            }
            ?: emptyList(),
        chapters = maybeChapters ?: filesAsChapters(),
        libraryId = item.libraryId,
        progress = progressResponse
            ?.let {
                MediaProgress(
                    currentTime = it.currentTime,
                    isFinished = it.isFinished,
                    lastUpdate = it.lastUpdate,
                )
            },
    )
}

fun libraryItemListToBookList(response: List<LibraryItem>): List<Book> {
    return response
        .mapNotNull {
            val title = it.media.metadata.title ?: return@mapNotNull null

            Book(
                id = it.id,
                title = title,
                author = it.media.metadata.authorName,
                cachedState = BookCachedState.ABLE_TO_CACHE,
                duration = it.media.duration.toInt(),
            )
        }
}

fun String.toLibraryType() = when (this) {
    "podcast" -> LibraryType.PODCAST
    "book" -> LibraryType.LIBRARY
    else -> LibraryType.UNKNOWN
}