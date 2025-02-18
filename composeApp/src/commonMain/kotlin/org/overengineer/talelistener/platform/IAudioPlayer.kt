package org.overengineer.talelistener.platform

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import org.overengineer.talelistener.common.AudioPlayerInitState
import org.overengineer.talelistener.domain.DetailedItem
import org.overengineer.talelistener.domain.TimerOption

/**
 * AudioPlayer Interface
 * This is somewhat the equivalent to the MediaRepository in Lissen
 */
interface IAudioPlayer {
    val isPlaying: Flow<Boolean>
    val timerOption: Flow<TimerOption?>
    val isPlaybackReady: Flow<Boolean>
    val isPlaybackPrepareError: Flow<Boolean>
    val totalPosition: Flow<Double>
    val playingBook: Flow<DetailedItem?>
    val playbackSpeed: Flow<Float>
    val currentChapterIndex: Flow<Int>
    val currentChapterPosition: Flow<Double>
    val currentChapterDuration: Flow<Double>

    fun getInitState(): AudioPlayerInitState
    fun updateTimer(timerOption: TimerOption?, position: Double? = null)
    fun rewind()
    fun forward()
    fun setChapter(index: Int)
    fun setChapterPosition(chapterPosition: Double)
    fun togglePlayPause()
    fun setPlaybackSpeed(factor: Float)
    suspend fun preparePlayback(bookId: String, fromBackground: Boolean = false)
    fun nextTrack()
    fun previousTrack(rewindRequired: Boolean = true)
    fun scheduleServiceTimer(delay: Double)
    fun cancelServiceTimer()
    fun startUpdatingProgress(detailedItem: DetailedItem)
    fun mediaPreparing()
    fun startPreparingPlayback(book: DetailedItem, fromBackground: Boolean)
    fun updateProgress(book: DetailedItem): Deferred<Unit>
    fun play()
    fun pause()
    fun seekTo(position: Double)

}