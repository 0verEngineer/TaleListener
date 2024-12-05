
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.audiobookshelf.podcast.converter
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Removed javax singleton stuff
 */

package org.overengineer.talelistener.channel.audiobookshelf.podcast.converter

import org.overengineer.talelistener.channel.audiobookshelf.podcast.model.PodcastItemsResponse
import org.overengineer.talelistener.domain.Book
import org.overengineer.talelistener.domain.BookCachedState
import org.overengineer.talelistener.domain.PagedItems

class PodcastPageResponseConverter constructor() {

    fun apply(response: PodcastItemsResponse): PagedItems<Book> = response
        .results
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
