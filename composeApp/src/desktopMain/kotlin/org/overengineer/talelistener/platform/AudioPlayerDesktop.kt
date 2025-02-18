package org.overengineer.talelistener.platform

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.overengineer.talelistener.common.AudioPlayerInitState
import org.overengineer.talelistener.content.TLMediaProvider
import org.overengineer.talelistener.domain.DetailedItem
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery
import uk.co.caprica.vlcj.media.MediaRef
import uk.co.caprica.vlcj.medialist.MediaList
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.list.MediaListPlayer
import uk.co.caprica.vlcj.player.list.MediaListPlayerEventAdapter


class AudioPlayerDesktop(
    private val preferences: TaleListenerSharedPreferences,
    mediaChannel: TLMediaProvider
) : AudioPlayer(mediaChannel) {

    private var mediaPlayerFactory: MediaPlayerFactory? = null
    private var mediaListPlayer: MediaListPlayer? = null
    private var mediaListMediaPlayer: MediaPlayer? = null
    private var mediaList: MediaList? = null
    private var isVlcFound = false

    private var timerJob: Job? = null

    private var currentPlayingIndex = 0

    init {
        isVlcFound = NativeDiscovery().discover()

        if (isVlcFound) {
            mediaPlayerFactory = MediaPlayerFactory()
            mediaListMediaPlayer = mediaPlayerFactory!!.mediaPlayers().newMediaPlayer()
            mediaListPlayer = mediaPlayerFactory!!.mediaPlayers().newMediaListPlayer()
            mediaListPlayer?.mediaPlayer()?.setMediaPlayer(mediaListMediaPlayer)
            mediaList = mediaPlayerFactory!!.media().newMediaList()
        }

        mediaListMediaPlayer?.events()?.addMediaPlayerEventListener(object : MediaPlayerEventAdapter() {
            override fun playing(mediaPlayer: MediaPlayer) {
                _isPlaying.value = true
            }

            override fun paused(mediaPlayer: MediaPlayer) {
                _isPlaying.value = false
            }

            override fun stopped(mediaPlayer: MediaPlayer) {
                _isPlaying.value = false
            }

            override fun timeChanged(mediaPlayer: MediaPlayer?, newTime: Long) {
                // Could be used for manual update
                //val test = mediaPlayer?.status()?.time()

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

        mediaListPlayer?.events()?.addMediaListPlayerEventListener(object : MediaListPlayerEventAdapter() {
            override fun mediaListPlayerFinished(mediaListPlayer: MediaListPlayer?) {
                _isPlaying.value = false
            }

            override fun nextItem(mediaListPlayer: MediaListPlayer?, item: MediaRef?) {
                currentPlayingIndex++
            }

            override fun stopped(mediaListPlayer: MediaListPlayer?) {
                _isPlaying.value = false
            }
        })

        // Restore default playback speed from preferences
        val lastSpeed = preferences.getPlaybackSpeed()
        _playbackSpeed.value = lastSpeed
        mediaListMediaPlayer?.controls()?.setRate(lastSpeed)
    }

    override fun getInitState(): AudioPlayerInitState {
        if (isVlcFound) {
            return AudioPlayerInitState.SUCCESS
        }
        return AudioPlayerInitState.DESKTOP_VLC_NOT_FOUND
    }

    override fun setPlaybackSpeed(factor: Float) {
        val speed = factor.coerceIn(0.5f, 3.0f)
        mediaListMediaPlayer?.controls()?.setRate(speed)

        _playbackSpeed.value = speed
        preferences.savePlaybackSpeed(speed)
    }

    override fun scheduleServiceTimer(delay: Double) {
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
        timerJob?.cancel()
        Napier.d("Timer cancelled")
    }

    override fun startUpdatingProgress(detailedItem: DetailedItem) {}

    override fun startPreparingPlayback(book: DetailedItem, fromBackground: Boolean) {
        if (_playingBook.value != book) {
            _totalPosition.value = 0.0
            _isPlaying.value = false

            playerScope.launch {
                preparePlaybackDesktop(book)
            }
        }
    }

    override fun updateProgress(book: DetailedItem): Deferred<Unit> {
        return CoroutineScope(Dispatchers.Main).async {  }
    }

    // seekTo has to be called before because of play/pause logic in vlc
    override fun play() {
        if (mediaListPlayer?.status()?.isPlaying == true) {
            return
        }

        mediaListMediaPlayer?.controls()?.setRate(preferences.getPlaybackSpeed())
        mediaListPlayer?.controls()?.setPause(false)
    }

    override fun pause() {
        mediaListPlayer?.controls()?.setPause(true)
    }

    override fun seekTo(position: Double) {
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

        val isPlaying = mediaListPlayer?.status()?.isPlaying ?: false

        // todo desktop-audio: If this happens the progress bar is always at the start instead of the end
        if (targetChapterIndex == -1) {
            val lastChapterIndex = book.files.size - 1
            val lastChapterDurationMs = durationMs.last()
            // play with index will return false if we don't call normal play before (only first time)
            mediaListPlayer?.controls()?.play()
            if (mediaListPlayer?.controls()?.play(lastChapterIndex) == true) {
                if (!isPlaying) {
                    mediaListPlayer?.controls()?.setPause(true)
                }

                mediaListMediaPlayer?.controls()?.setTime(lastChapterDurationMs)
                _totalPosition.value = cumulativeDurationMs.last().toDouble() // todo desktop-audio: test, is this wrong and the issue of the progress bar?
                currentPlayingIndex = lastChapterIndex
            }
            return
        }

        val chapterStartTimeMs = cumulativeDurationMs[targetChapterIndex - 1]
        val chapterProgressMs = positionMs - chapterStartTimeMs

        // todo desktop-audio: prefetch stream error on progress bar drag

        // todo desktop-audio: open vlc issue for this play stuff: https://code.videolan.org/videolan/vlc/-/issues
        // todo desktop-audio: this leads also to multiple vlc errors/warnings
        // play with index will return false if we don't call normal play before (only first time)
        mediaListPlayer?.controls()?.play()
        val playSuccess = mediaListPlayer?.controls()?.play(targetChapterIndex -1)
        if (playSuccess == true) {
            if (!isPlaying) {
                mediaListPlayer?.controls()?.setPause(true)
            }

            mediaListMediaPlayer?.controls()?.setTime(chapterProgressMs)
            _totalPosition.value = safePosition
            currentPlayingIndex = targetChapterIndex - 1
        }
    }

    private suspend fun preparePlaybackDesktop(book: DetailedItem) {
        withContext(Dispatchers.IO) {
            _isPlaybackPrepareError.value = false
            val prepareQueue = async {
                mediaList?.media()?.clear()

                book.files.forEach { file ->
                    val mrl = mediaChannel.provideFileUrl(book.id, file.id).toString()
                    if (mediaList?.media()?.add(mrl) != true) {
                        _isPlaybackPrepareError.value = true
                    }
                }

                mediaListPlayer?.list()?.setMediaList(mediaList?.newMediaListRef())
            }

            val prepareSession = async {
                // todo desktop-audio: make the service abstract
                // todo desktop-audio: only start if _isPlaybackPrepareError is false
                //playbackSynchronizationService.startPlaybackSynchronization(book)
            }

            awaitAll(prepareSession, prepareQueue)

            if (!_isPlaybackPrepareError.value) {
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
}