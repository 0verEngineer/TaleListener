
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.widget.MediaRepository
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Migrated to kotlin multiplatform
 * - Split the code into android specific (AudioPlayerAndroid) and common (AudioPlayer)
 */

package org.overengineer.talelistener.platform

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import org.overengineer.talelistener.content.TLMediaProvider
import org.overengineer.talelistener.domain.CurrentEpisodeTimerOption
import org.overengineer.talelistener.domain.DetailedItem
import org.overengineer.talelistener.domain.DurationTimerOption
import org.overengineer.talelistener.domain.TimerOption
import org.overengineer.talelistener.playback.service.calculateChapterIndex
import org.overengineer.talelistener.playback.service.calculateChapterPosition


abstract class AudioPlayer(
    private val mediaChannel: TLMediaProvider
) : IAudioPlayer {
    val playerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    protected val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> get() = _isPlaying.asStateFlow()

    protected val _timerOption = MutableStateFlow<TimerOption?>(null)
    override val timerOption: StateFlow<TimerOption?> get() = _timerOption.asStateFlow()

    protected val _isPlaybackReady = MutableStateFlow(false)
    override val isPlaybackReady: StateFlow<Boolean> get() = _isPlaybackReady.asStateFlow()

    protected val _totalPosition = MutableStateFlow(0.0)
    override val totalPosition: StateFlow<Double> get() = _totalPosition.asStateFlow()

    protected val _playingBook = MutableStateFlow<DetailedItem?>(null)
    override val playingBook: StateFlow<DetailedItem?> get() = _playingBook.asStateFlow()

    protected val _playbackSpeed = MutableStateFlow(0.0f)
    override val playbackSpeed: StateFlow<Float> get() = _playbackSpeed.asStateFlow()

    override val currentChapterIndex = combine(
        _totalPosition,
        _playingBook
    ) { totalPos, maybeBook ->
        val book = maybeBook ?: return@combine 0
        calculateChapterIndex(book, totalPos)
    }.stateIn(
        scope = playerScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    override val currentChapterPosition = combine(
        _totalPosition,
        _playingBook
    ) { totalPos, maybeBook ->
        val book = maybeBook ?: return@combine 0.0
        calculateChapterPosition(book, totalPos)
    }.stateIn(
        scope = playerScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    override val currentChapterDuration = combine(
        _totalPosition,
        _playingBook
    ) { totalPos, maybeBook ->
        val book = maybeBook ?: return@combine 0.0
        val chapterIndex = calculateChapterIndex(book, totalPos)
        book.chapters.getOrNull(chapterIndex)?.duration ?: 0.0
    }.stateIn(
        scope = playerScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    override fun updateTimer(timerOption: TimerOption?, position: Double?) {
        _timerOption.value = timerOption

        when (timerOption) {
            is DurationTimerOption -> scheduleServiceTimer(timerOption.duration * 60.0)

            is CurrentEpisodeTimerOption -> {
                val playingBook = playingBook.value ?: return
                val currentPosition = position ?: totalPosition.value

                val chapterDuration = calculateChapterIndex(playingBook, currentPosition)
                    .let { playingBook.chapters[it] }
                    .duration

                val chapterPosition = calculateChapterPosition(
                    book = playingBook,
                    overallPosition = currentPosition,
                )

                scheduleServiceTimer(chapterDuration - chapterPosition)
            }

            null -> cancelServiceTimer()
        }
    }

    override fun rewind() {
        totalPosition
            .value
            .let { seekTo(it - 10L) }
    }

    override fun forward() {
        totalPosition
            .value
            .let { seekTo(it + 30L) }
    }

    override fun setChapter(index: Int) {
        val book = playingBook.value ?: return
        try {
            val chapterStartsAt = book
                .chapters[index]
                .start

            seekTo(chapterStartsAt)
        } catch (ex: Exception) {
            return
        }
    }

    override fun setChapterPosition(chapterPosition: Double) {
        val book = playingBook.value ?: return
        val overallPosition = totalPosition.value

        val currentIndex = calculateChapterIndex(book, overallPosition)

        if (currentIndex < 0) {
            return
        }

        try {
            val absolutePosition = currentIndex
                .let { chapterIndex -> book.chapters[chapterIndex].start }
                .let { it + chapterPosition }

            seekTo(absolutePosition)
        } catch (ex: Exception) {
            return
        }
    }

    override fun togglePlayPause() {
        when (isPlaying.value) {
            true -> pause()
            else -> play()
        }
    }

    override suspend fun preparePlayback(bookId: String, fromBackground: Boolean) {
        mediaPreparing()

        coroutineScope {
            withContext(Dispatchers.IO) {
                mediaChannel
                    .fetchBook(bookId)
                    .foldAsync(
                        onSuccess = { startPreparingPlayback(it, fromBackground) },
                        onFailure = {},
                    )
            }
        }
    }

    override fun nextTrack() {
        val book = playingBook.value ?: return
        val overallPosition = totalPosition.value

        val currentIndex = calculateChapterIndex(book, overallPosition)

        val nextChapterIndex = currentIndex + 1
        setChapter(nextChapterIndex)
    }

    override fun previousTrack(rewindRequired: Boolean) {
        val book = playingBook.value ?: return
        val overallPosition = totalPosition.value

        val currentIndex = calculateChapterIndex(book, overallPosition)
        val chapterPosition = calculateChapterPosition(
            book = book,
            overallPosition = overallPosition,
        )

        val currentIndexReplay = (chapterPosition > CURRENT_TRACK_REPLAY_THRESHOLD || currentIndex == 0)

        when {
            currentIndexReplay && rewindRequired -> setChapter(currentIndex)
            currentIndex > 0 -> setChapter(currentIndex - 1)
        }
    }

    override fun mediaPreparing() {
        timerOption
            .value
            ?.let { updateTimer(timerOption = null) }

        _isPlaybackReady.value = false
    }

    companion object {
        const val CURRENT_TRACK_REPLAY_THRESHOLD = 5
    }
}