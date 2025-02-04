
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.playback.service
 * Modifications:
 * - Updated package statement and adjusted imports.
 */

package org.overengineer.talelistener.playback.service

import org.overengineer.talelistener.domain.DetailedItem

fun calculateChapterPosition(book: DetailedItem, overallPosition: Double): Double {
    var accumulatedDuration = 0.0

    for (chapter in book.chapters) {
        val chapterEnd = accumulatedDuration + chapter.duration
        if (overallPosition < chapterEnd - 0.1) {
            return (overallPosition - accumulatedDuration)
        }
        accumulatedDuration = chapterEnd
    }

    return 0.0
}
