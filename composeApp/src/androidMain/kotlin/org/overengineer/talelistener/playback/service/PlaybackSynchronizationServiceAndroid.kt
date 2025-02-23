
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.playback.service
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Updated to koin di
 */

package org.overengineer.talelistener.playback.service

import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.overengineer.talelistener.content.TLMediaProvider
import org.overengineer.talelistener.domain.DetailedItem
import org.overengineer.talelistener.domain.PlaybackProgress
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences

class PlaybackSynchronizationServiceAndroid (
    private val exoPlayer: ExoPlayer,
    mediaChannel: TLMediaProvider,
    sharedPreferences: TaleListenerSharedPreferences,
) : PlaybackSynchronizationService(mediaChannel, sharedPreferences) {

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    scheduleSynchronization()
                } else {
                    executeSynchronization()
                }
            }
        })
    }

    override fun scheduleSynchronization() {
        serviceScope
            .launch {
                if (exoPlayer.isPlaying) {
                    executeSynchronization()
                    delay(SYNC_INTERVAL)
                    scheduleSynchronization()
                }
            }
    }

    override fun executeSynchronization() {
        val elapsedMs = exoPlayer.currentPosition
        val overallProgress = getProgress(elapsedMs)

        serviceScope
            .launch(Dispatchers.IO) {
                playbackSession
                    ?.takeIf { it.bookId == currentBook?.id }
                    ?.let { synchronizeProgress(it, overallProgress) }
                    ?: openPlaybackSession(overallProgress)
            }
    }

    override fun getProgress(currentElapsedMs: Long): PlaybackProgress {
        val currentBook = exoPlayer
            .currentMediaItem
            ?.localConfiguration
            ?.tag as? DetailedItem
            ?: return PlaybackProgress(0.0, 0.0)

        val currentIndex = exoPlayer.currentMediaItemIndex

        val previousDuration = currentBook.files
            .take(currentIndex)
            .sumOf { it.duration * 1000 }

        val totalDuration = currentBook.files.sumOf { it.duration * 1000 }

        val totalElapsedMs = previousDuration + currentElapsedMs

        return PlaybackProgress(
            currentTime = totalElapsedMs / 1000.0,
            totalTime = totalDuration / 1000.0,
        )
    }
}
