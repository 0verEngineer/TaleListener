package org.overengineer.talelistener.playback.service

import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.overengineer.talelistener.content.TLMediaProvider
import org.overengineer.talelistener.domain.PlaybackProgress
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences
import org.overengineer.talelistener.platform.AudioPlayerDesktop
import uk.co.caprica.vlcj.player.base.MediaPlayer

class PlaybackSynchronizationServiceDesktop(
    mediaChannel: TLMediaProvider,
    sharedPreferences: TaleListenerSharedPreferences,
    private val mediaPlayer: MediaPlayer,
    private val audioPlayerDesktop: AudioPlayerDesktop
) : PlaybackSynchronizationService(mediaChannel, sharedPreferences) {

    override fun scheduleSynchronization() {
        serviceScope.launch {
            if (mediaPlayer.status().isPlaying) {
                executeSynchronization()
                delay(SYNC_INTERVAL)
                scheduleSynchronization()
            }
        }
    }

    override fun executeSynchronization() {
        val elapsedMs = mediaPlayer.status().time()
        Napier.d("executeSynchronization() elapsedMs: $elapsedMs")
        val overallProgress = getProgress(elapsedMs)
        Napier.d("executeSynchronization() overallProgress.currentTime: ${overallProgress.currentTime}; overallProgress.totalTime: ${overallProgress.totalTime}")

        serviceScope.launch(Dispatchers.IO) {
            playbackSession
                ?.takeIf { it.bookId == currentBook?.id }
                ?.let { synchronizeProgress(it, overallProgress) }
                ?:openPlaybackSession(overallProgress)
        }
    }

    override fun getProgress(currentElapsedMs: Long): PlaybackProgress {
        val currentBook = audioPlayerDesktop.playingBook.value
            ?: return PlaybackProgress(0.0, 0.0)

        val currentIndex = audioPlayerDesktop.currentChapterIndex.value

        val previousDuration = currentBook.files
            .take(currentIndex)
            .sumOf{ it.duration * 1000 }

        val totalDuration = currentBook.files.sumOf { it.duration * 1000 }

        val totalElapsedMs = previousDuration + currentElapsedMs

        return PlaybackProgress(
            currentTime = totalElapsedMs / 1000.0,
            totalTime = totalDuration / 1000.0
        )
    }

    fun playing() {
        scheduleSynchronization()
    }

    fun paused() {
        executeSynchronization()
    }

    fun finished() {
        executeSynchronization()
    }
}
