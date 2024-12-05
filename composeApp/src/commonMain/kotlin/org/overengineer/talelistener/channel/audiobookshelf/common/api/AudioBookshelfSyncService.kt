
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.audiobookshelf.common.api
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Removed javax singleton stuff
 */

package org.overengineer.talelistener.channel.audiobookshelf.common.api

import org.overengineer.talelistener.channel.audiobookshelf.common.model.playback.ProgressSyncRequest
import org.overengineer.talelistener.channel.common.ApiResult
import org.overengineer.talelistener.domain.PlaybackProgress

class AudioBookshelfSyncService constructor(
    private val dataRepository: AudioBookshelfDataRepository,
) {

    private var previousItemId: String? = null
    private var previousTrackedTime: Double = 0.0

    suspend fun syncProgress(
        itemId: String,
        progress: PlaybackProgress,
    ): ApiResult<Unit> {
        val trackedTime = previousTrackedTime
            .takeIf { itemId == previousItemId }
            ?.let { progress.currentTime - previousTrackedTime }
            ?.toInt()
            ?: 0

        val request = ProgressSyncRequest(
            currentTime = progress.currentTime,
            duration = progress.totalTime,
            timeListened = trackedTime,
        )

        return dataRepository
            .publishLibraryItemProgress(itemId, request)
            .also {
                previousTrackedTime = progress.currentTime
                previousItemId = itemId
            }
    }
}
