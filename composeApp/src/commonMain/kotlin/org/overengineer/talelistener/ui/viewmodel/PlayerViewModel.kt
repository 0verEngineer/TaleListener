
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.ui.viewmodel
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Migrated to kotlin multiplatform
 * - Added _audioPlayerInitState and init for vlc discovery on desktop
 */

package org.overengineer.talelistener.ui.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.overengineer.talelistener.common.AudioPlayerInitState
import org.overengineer.talelistener.domain.BookChapter
import org.overengineer.talelistener.domain.DetailedItem
import org.overengineer.talelistener.domain.TimerOption
import org.overengineer.talelistener.platform.AudioPlayer

class PlayerViewModel(
    private val audioPlayer: AudioPlayer
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _audioPlayerInitState = MutableStateFlow<AudioPlayerInitState?>(null)
    val audioPlayerInitState: StateFlow<AudioPlayerInitState?> = _audioPlayerInitState.asStateFlow()

    val book: StateFlow<DetailedItem?> = audioPlayer.playingBook

    val currentChapterIndex: StateFlow<Int> = audioPlayer.currentChapterIndex
    val currentChapterPosition: StateFlow<Double> = audioPlayer.currentChapterPosition
    val currentChapterDuration: StateFlow<Double> = audioPlayer.currentChapterDuration

    val timerOption: StateFlow<TimerOption?> = audioPlayer.timerOption

    private val _playingQueueExpanded = MutableStateFlow(false)
    val playingQueueExpanded: StateFlow<Boolean> = _playingQueueExpanded.asStateFlow()

    val isPlaybackReady: StateFlow<Boolean> = audioPlayer.isPlaybackReady
    val isPlaybackPrepareError: StateFlow<Boolean> = audioPlayer.isPlaybackPrepareError
    val playbackSpeed: StateFlow<Float> = audioPlayer.playbackSpeed

    private val _searchRequested = MutableStateFlow(false)
    val searchRequested: StateFlow<Boolean> = _searchRequested.asStateFlow()

    private val _searchToken = MutableStateFlow("")
    val searchToken: StateFlow<String> = _searchToken.asStateFlow()

    val isPlaying: StateFlow<Boolean> = audioPlayer.isPlaying

    init {
        _audioPlayerInitState.value = audioPlayer.getInitState()
    }

    fun expandPlayingQueue() {
        _playingQueueExpanded.value = true
    }

    fun setTimer(option: TimerOption?) {
        audioPlayer.updateTimer(option)
    }

    fun collapsePlayingQueue() {
        _playingQueueExpanded.value = false
    }

    fun togglePlayingQueue() {
        _playingQueueExpanded.value = !(_playingQueueExpanded.value ?: false)
    }

    fun requestSearch() {
        _searchRequested.value = true
    }

    fun dismissSearch() {
        _searchRequested.value = false
        _searchToken.value = ""
    }

    fun updateSearch(token: String) {
        _searchToken.value = token
    }

    fun preparePlayback(bookId: String) {
        viewModelScope.launch { audioPlayer.preparePlayback(bookId) }
    }

    fun rewind() {
        audioPlayer.rewind()
    }

    fun forward() {
        audioPlayer.forward()
    }

    fun seekTo(chapterPosition: Double) {
        audioPlayer.setChapterPosition(chapterPosition)
    }

    fun setChapter(chapter: BookChapter) {
        val index = book.value?.chapters?.indexOf(chapter) ?: -1
        audioPlayer.setChapter(index)
    }

    fun setPlaybackSpeed(factor: Float) = audioPlayer.setPlaybackSpeed(factor)

    fun nextTrack() = audioPlayer.nextTrack()

    fun previousTrack() = audioPlayer.previousTrack()

    fun togglePlayPause() = audioPlayer.togglePlayPause()}