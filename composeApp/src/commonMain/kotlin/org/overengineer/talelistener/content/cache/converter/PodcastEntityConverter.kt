
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * This file includes code of all converters in:
 *   - org.grakovne.lissen.channel.audiobookshelf.podcast.converter
 */

package org.overengineer.talelistener.content.cache.converter

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import org.overengineer.talelistener.channel.audiobookshelf.common.model.MediaProgressResponse
import org.overengineer.talelistener.channel.audiobookshelf.podcast.model.PodcastEpisodeResponse
import org.overengineer.talelistener.channel.audiobookshelf.podcast.model.PodcastItem
import org.overengineer.talelistener.channel.audiobookshelf.podcast.model.PodcastItemsResponse
import org.overengineer.talelistener.channel.audiobookshelf.podcast.model.PodcastResponse
import org.overengineer.talelistener.domain.Book
import org.overengineer.talelistener.domain.BookCachedState
import org.overengineer.talelistener.domain.BookChapter
import org.overengineer.talelistener.domain.BookFile
import org.overengineer.talelistener.domain.DetailedItem
import org.overengineer.talelistener.domain.MediaProgress
import org.overengineer.talelistener.domain.PagedItems

fun podcastItemsResponseToBookPagedItems(response: PodcastItemsResponse): PagedItems<Book> {
    return response.results
        .mapNotNull {
            val title = it.media.metadata.title ?: return@mapNotNull null

            Book(
                id = it.id,
                title = title,
                author = it.media.metadata.author,
                cachedState = BookCachedState.UNABLE_TO_CACHE,
                duration = it.media.duration.toInt(),
            )
        }
        .let {
            PagedItems(
                items = it,
                currentPage = response.page,
            )
        }
}

fun podcastResponseAndMediaProgressResponseToDetailedItem(
    item: PodcastResponse,
    progressResponse: MediaProgressResponse? = null
): DetailedItem {
    val orderedEpisodes = item
        .media
        .episodes
    //?.orderEpisode() // todo

    val filesAsChapters: List<BookChapter> =
        orderedEpisodes
            ?.fold(0.0 to mutableListOf<BookChapter>()) { (accDuration, chapters), file ->
                chapters.add(
                    BookChapter(
                        start = accDuration,
                        end = accDuration + file.audioFile.duration,
                        title = file.title,
                        duration = file.audioFile.duration,
                        id = file.id,
                    ),
                )
                accDuration + file.audioFile.duration to chapters
            }
            ?.second
            ?: emptyList()

    return DetailedItem(
        id = item.id,
        title = item.media.metadata.title,
        libraryId = item.libraryId,
        author = item.media.metadata.author,
        files = orderedEpisodes
            ?.map {
                BookFile(
                    id = it.audioFile.ino,
                    name = it.title,
                    duration = it.audioFile.duration,
                    mimeType = it.audioFile.mimeType,
                )
            }
            ?: emptyList(),
        chapters = filesAsChapters,
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

fun podcastItemListToBookList(response: List<PodcastItem>): List<Book> {
    return response
        .mapNotNull {
            val title = it.media.metadata.title ?: return@mapNotNull null

            Book(
                id = it.id,
                title = title,
                author = it.media.metadata.author,
                cachedState = BookCachedState.UNABLE_TO_CACHE,
                duration = it.media.duration.toInt(),
            )
        }
}



private const val DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss Z"

private fun List<PodcastEpisodeResponse>.orderEpisode() =
    this.sortedWith(
        compareBy<PodcastEpisodeResponse> { item ->
            try {
                item.pubDate?.let { parseDate(it)?.toEpochMilliseconds() }
            } catch (e: Exception) {
                null
            }
        }
            .thenBy { it.season.safeToInt() }
            .thenBy { it.episode.safeToInt() },
    )

private fun parseDate(dateString: String): Instant? {
    return try {
        // todo match the DATE_FORMAT above or investigate why this is like this
        val format = LocalDateTime.Format {
            year()
            char('-')
            monthNumber()
            char('-')
            dayOfMonth()

            char(' ')

            hour()
            char(':')
            minute()
            char(':')
            second()
        }
        LocalDateTime.parse(dateString, format).toInstant(TimeZone.UTC)
    } catch (e: Exception) {
        null
    }
}

private fun String?.safeToInt(): Int? {
    val maybeNumber = this?.takeIf { it.isNotBlank() }

    return try {
        maybeNumber?.toInt()
    } catch (ex: Exception) {
        null
    }
}