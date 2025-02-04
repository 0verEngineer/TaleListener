
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.playback.service
 * Modifications:
 * - Updated package statement and adjusted imports.
 */

package org.overengineer.talelistener.playback.service

import org.overengineer.talelistener.domain.DetailedItem

fun calculateChapterIndex(item: DetailedItem, totalPosition: Double): Int {
    var accumulatedDuration = 0.0

    for ((index, chapter) in item.chapters.withIndex()) {
        accumulatedDuration += chapter.duration
        if (totalPosition < accumulatedDuration - 0.1) {
            return index
        }
    }

    return item.chapters.size - 1
}
