package org.overengineer.talelistener.platform

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.overengineer.talelistener.common.AudioPlayerInitState
import org.overengineer.talelistener.content.TLMediaProvider
import org.overengineer.talelistener.domain.DetailedItem
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences
import org.overengineer.talelistener.playback.service.PlaybackSynchronizationServiceDesktop
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter


class AudioPlayerDesktop(
    private val preferences: TaleListenerSharedPreferences,
    mediaChannel: TLMediaProvider
) : AudioPlayer(mediaChannel) {

    private var playbackSynchronizationService: PlaybackSynchronizationServiceDesktop? = null
    private var mediaPlayerFactory: MediaPlayerFactory? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isVlcFound = false

    private var timerJob: Job? = null

    private var currentPlayingIndex = 0

    // Is set to true in seekTo to disable event handling for the play and then pause calls in seekTo
    private var isPlayingEventFromSeekTo = false
    private var isPausedEventFromSeekTo = false

    init {
        // todo launch async because it is very slow on windows if not found
        Napier.d("NativeDiscovery starting")
        isVlcFound = NativeDiscovery().discover()
        Napier.d("NativeDiscovery finished, found: $isVlcFound")

        if (isVlcFound) {
            mediaPlayerFactory = MediaPlayerFactory()
            mediaPlayer = mediaPlayerFactory!!.mediaPlayers().newMediaPlayer()
            playbackSynchronizationService = PlaybackSynchronizationServiceDesktop(
                mediaChannel = mediaChannel,
                sharedPreferences = preferences,
                mediaPlayer = mediaPlayer!!,
                audioPlayerDesktop = this
            )
        }

        mediaPlayer?.events()?.addMediaPlayerEventListener(object : MediaPlayerEventAdapter() {
            override fun playing(mediaPlayer: MediaPlayer) {
                if (isPlayingEventFromSeekTo) {
                    isPlayingEventFromSeekTo = false
                    return
                }
                Napier.d("playing event")
                _isPlaying.value = true
                playbackSynchronizationService?.playing()
            }

            override fun paused(mediaPlayer: MediaPlayer) {
                if (isPausedEventFromSeekTo) {
                    isPausedEventFromSeekTo = false
                    return
                }
                Napier.d("paused event")
                _isPlaying.value = false
                playbackSynchronizationService?.paused()
            }

            override fun finished(mediaPlayer: MediaPlayer?) {
                val book = _playingBook.value ?: return

                if (currentPlayingIndex >= book.files.size) {
                    Napier.d("finished event - end of book reached")
                    _isPlaying.value = false
                    playbackSynchronizationService?.finished()

                } else {
                    currentPlayingIndex++
                    mediaPlayer?.submit {
                        val playing = mediaPlayer.media().play(getMrlForIndex(currentPlayingIndex, book))
                        Napier.d("finished event - next playing: $playing")
                    }
                }
            }

            override fun timeChanged(mediaPlayer: MediaPlayer?, newTime: Long) {
                CoroutineScope(Dispatchers.Main).launch {
                    val files = playingBook.value?.files
                    if (files == null) {
                        Napier.w("files is null, skipping update")
                        return@launch
                    }
                    val accumulated = files.take(currentPlayingIndex).sumOf { it.duration }
                    val currentFilePosition = newTime / 1000.0

                    _totalPosition.value = (accumulated + currentFilePosition)
                }
            }
        })

        // Restore default playback speed from preferences
        val lastSpeed = preferences.getPlaybackSpeed()
        _playbackSpeed.value = lastSpeed
        mediaPlayer?.controls()?.setRate(lastSpeed)
    }

    override fun getInitState(): AudioPlayerInitState {
        Napier.d("getInitState()")
        if (isVlcFound) {
            return AudioPlayerInitState.SUCCESS
        }
        return AudioPlayerInitState.DESKTOP_VLC_NOT_FOUND
    }

    override fun setPlaybackSpeed(factor: Float) {
        Napier.d("setPlaybackSpeed($factor)")
        val speed = factor.coerceIn(0.5f, 3.0f)
        mediaPlayer?.controls()?.setRate(speed)

        _playbackSpeed.value = speed
        preferences.savePlaybackSpeed(speed)
    }

    override fun scheduleServiceTimer(delay: Double) {
        Napier.d("scheduleServiceTimer($delay)")
        if (delay < 0) {
            return
        }

        val delayMs = delay * 1000

        timerJob?.cancel()

        timerJob = playerScope.launch {
            while (isActive) {
                delay(delayMs.toLong())
                pause()
                _timerOption.value = null
            }
        }
        Napier.d("Timer started for $delayMs ms")
    }

    override fun cancelServiceTimer() {
        Napier.d("cancelServiceTimer()")
        timerJob?.cancel()
    }

    override fun startUpdatingProgress(detailedItem: DetailedItem) {
        Napier.d("startUpdatingProgress noop called with book: ${detailedItem.title}; id: $detailedItem.id")
    }

    override fun startPreparingPlayback(book: DetailedItem, fromBackground: Boolean) {
        Napier.d("startPreparingPlayback book: ${book.title}; fromBackground: $fromBackground")
        if (_playingBook.value != book) {
            _totalPosition.value = 0.0
            _isPlaying.value = false

            playerScope.launch {
                preparePlaybackDesktop(book)
            }
        }
    }

    override fun updateProgress(book: DetailedItem): Deferred<Unit> {
        Napier.d("updateProgress noop called with book: ${book.title}")
        return CoroutineScope(Dispatchers.Main).async {  }
    }

    // seekTo has to be called before because of play/pause logic in vlc
    override fun play() {
        val isPlaying = mediaPlayer?.status()?.isPlaying ?: false
        Napier.d("play() - isPlaying: $isPlaying")

        if (isPlaying) {
            return
        }

        mediaPlayer?.controls()?.setRate(preferences.getPlaybackSpeed())
        mediaPlayer?.controls()?.setPause(false)
    }

    override fun pause() {
        Napier.d("pause()")
        mediaPlayer?.controls()?.setPause(true)
    }

    override fun seekTo(position: Double) {
        Napier.d("seekTo($position)")
        val book = playingBook.value ?: return

        val overallDuration = book
            .chapters
            .sumOf { it.duration }

        val safePosition = minOf(overallDuration, maxOf(0.0, position))

        if (book.files.isEmpty()) {
            Napier.w("Tried to seek position $safePosition in the empty book, skipping")
            return
        }

        val positionMs = (position * 1000).toLong()

        val durationMs = book.files.map { (it.duration * 1000).toLong() }
        val cumulativeDurationMs = durationMs.runningFold(0L) { acc, duration -> acc + duration }

        val targetChapterIndex = cumulativeDurationMs.indexOfFirst { it > positionMs }

        val isPlaying = mediaPlayer?.status()?.isPlaying ?: false

        isPausedEventFromSeekTo = true
        isPlayingEventFromSeekTo = true

        if (targetChapterIndex == -1) {
            val lastChapterIndex = book.files.size - 1
            val lastChapterDurationMs = durationMs.last()

            mediaPlayer?.media()?.play(getMrlForIndex(lastChapterIndex, book))
            mediaPlayer?.controls()?.setTime(lastChapterDurationMs)
            mediaPlayer?.controls()?.pause()
            _totalPosition.value = (cumulativeDurationMs.last() / 1000).toDouble()
            currentPlayingIndex = lastChapterIndex
            _isPlaying.value = false

            return
        }

        val chapterStartTimeMs = cumulativeDurationMs[targetChapterIndex - 1]
        val chapterProgressMs = positionMs - chapterStartTimeMs

        mediaPlayer?.media()?.play(getMrlForIndex(targetChapterIndex - 1, book))

        if (!isPlaying) {
            // We play and then pause to later use setPause(false) to play with the correct time
            mediaPlayer?.controls()?.setPause(true)
        }

        mediaPlayer?.controls()?.setTime(chapterProgressMs)
        _totalPosition.value = safePosition
        currentPlayingIndex = targetChapterIndex - 1
    }

    private fun getMrlForIndex(index: Int, book: DetailedItem) : String {
        val file = book.files[index]
        return mediaChannel.provideFileUrl(book.id, file.id).toString()
    }

    private suspend fun preparePlaybackDesktop(book: DetailedItem) {
        Napier.d("preparePlaybackDesktop book: ${book.title}")
        withContext(Dispatchers.IO) {
            _isPlaybackPrepareError.value = false

            mediaPlayer?.controls()?.stop()
            mediaPlayer?.media()?.prepare(getMrlForIndex(0, book))

            if (!_isPlaybackPrepareError.value) {
                playbackSynchronizationService?.preparePlaybackSynchronization(book)

                withContext(Dispatchers.Main) {
                    _playingBook.value = book

                    val currentTime = book.progress?.currentTime
                    if (currentTime == null) {
                        seekTo(0.0)
                    } else {
                        seekTo(currentTime)
                    }

                    _isPlaybackReady.value = true
                }
            }
        }
    }

    fun release() {
        timerJob?.cancel()
        mediaPlayer?.release()
        mediaPlayerFactory?.release()
    }
}