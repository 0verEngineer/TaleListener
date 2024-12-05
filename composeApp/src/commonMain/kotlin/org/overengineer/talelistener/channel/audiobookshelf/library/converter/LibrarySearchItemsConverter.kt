
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.audiobookshelf.library.converter
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Removed javax singleton stuff
 */

package org.overengineer.talelistener.channel.audiobookshelf.library.converter

import org.overengineer.talelistener.channel.audiobookshelf.library.model.LibraryItem
import org.overengineer.talelistener.domain.Book
import org.overengineer.talelistener.domain.BookCachedState

class LibrarySearchItemsConverter constructor() {
    fun apply(response: List<LibraryItem>): List<Book> {
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
}
