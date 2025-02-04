
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
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.overengineer.talelistener.content.TLMediaProvider
import org.overengineer.talelistener.domain.DetailedItem
import org.overengineer.talelistener.domain.PlaybackProgress
import org.overengineer.talelistener.domain.PlaybackSession
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences

// todo: interface / abstract just like the AudioPlayer? -> check how it will work IOS an desktop
class PlaybackSynchronizationService (
    private val exoPlayer: ExoPlayer,
    private val mediaChannel: TLMediaProvider,
    private val sharedPreferences: TaleListenerSharedPreferences,
) {

    private var currentBook: DetailedItem? = null
    private var currentChapterIndex: Int? = null
    private var playbackSession: PlaybackSession? = null
    private val serviceScope = MainScope()

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

    fun startPlaybackSynchronization(book: DetailedItem) {
        serviceScope.coroutineContext.cancelChildren()
        currentBook = book
    }

    private fun scheduleSynchronization() {
        serviceScope
            .launch {
                if (exoPlayer.isPlaying) {
                    executeSynchronization()
                    delay(SYNC_INTERVAL)
                    scheduleSynchronization()
                }
            }
    }

    private fun executeSynchronization() {
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

    private suspend fun synchronizeProgress(
        it: PlaybackSession,
        overallProgress: PlaybackProgress,
    ): Unit? {
        val currentIndex = currentBook
            ?.let { calculateChapterIndex(it, overallProgress.currentTime) }
            ?: 0

        if (currentIndex != currentChapterIndex) {
            openPlaybackSession(overallProgress)
            currentChapterIndex = currentIndex
        }

        return mediaChannel
            .syncProgress(
                sessionId = it.sessionId,
                bookId = it.bookId,
                progress = overallProgress,
            )
            .foldAsync(
                onSuccess = {},
                onFailure = { openPlaybackSession(overallProgress) },
            )
    }

    private suspend fun openPlaybackSession(overallProgress: PlaybackProgress) = currentBook
        ?.let { book ->
            val chapterIndex = calculateChapterIndex(book, overallProgress.currentTime)
            mediaChannel
                .startPlayback(
                    bookId = book.id,
                    deviceId = sharedPreferences.getDeviceId(),
                    supportedMimeTypes = MimeTypeProvider.getSupportedMimeTypes(),
                    chapterId = book.chapters[chapterIndex].id,
                )
                .fold(
                    onSuccess = { playbackSession = it },
                    onFailure = {},
                )
        }

    private fun getProgress(currentElapsedMs: Long): PlaybackProgress {
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

    companion object {

        private const val SYNC_INTERVAL = 30_000L
    }
}
