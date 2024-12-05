
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.audiobookshelf.library.converter
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Removed javax singleton stuff
 */

package org.overengineer.talelistener.channel.audiobookshelf.library.converter

import org.overengineer.talelistener.channel.audiobookshelf.common.model.MediaProgressResponse
import org.overengineer.talelistener.channel.audiobookshelf.library.model.BookResponse
import org.overengineer.talelistener.channel.audiobookshelf.library.model.LibraryAuthorResponse
import org.overengineer.talelistener.domain.BookChapter
import org.overengineer.talelistener.domain.BookFile
import org.overengineer.talelistener.domain.DetailedItem
import org.overengineer.talelistener.domain.MediaProgress

class BookResponseConverter constructor() {

    fun apply(
        item: BookResponse,
        progressResponse: MediaProgressResponse? = null,
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
}
