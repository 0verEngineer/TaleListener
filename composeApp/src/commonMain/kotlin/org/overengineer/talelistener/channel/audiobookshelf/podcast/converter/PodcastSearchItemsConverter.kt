
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.audiobookshelf.podcast.converter
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Removed javax singleton stuff
 */

package org.overengineer.talelistener.channel.audiobookshelf.podcast.converter

import org.overengineer.talelistener.channel.audiobookshelf.podcast.model.PodcastItem
import org.overengineer.talelistener.domain.Book
import org.overengineer.talelistener.domain.BookCachedState

class PodcastSearchItemsConverter constructor() {
    fun apply(response: List<PodcastItem>): List<Book> {
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
}
