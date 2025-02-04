
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

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.MoreExecutors
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.overengineer.talelistener.content.TLMediaProvider
import org.overengineer.talelistener.domain.CurrentEpisodeTimerOption
import org.overengineer.talelistener.domain.DetailedItem
import org.overengineer.talelistener.domain.DurationTimerOption
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences
import org.overengineer.talelistener.playback.service.PlaybackService
import org.overengineer.talelistener.playback.service.PlaybackService.Companion.ACTION_SEEK_TO
import org.overengineer.talelistener.playback.service.PlaybackService.Companion.BOOK_EXTRA
import org.overengineer.talelistener.playback.service.PlaybackService.Companion.PLAYBACK_READY
import org.overengineer.talelistener.playback.service.PlaybackService.Companion.POSITION
import org.overengineer.talelistener.playback.service.PlaybackService.Companion.TIMER_EXPIRED
import org.overengineer.talelistener.playback.service.PlaybackService.Companion.TIMER_VALUE_EXTRA

class AudioPlayerAndroid(
    private val context: Context,
    private val preferences: TaleListenerSharedPreferences,
    mediaChannel: TLMediaProvider
) : AudioPlayer(mediaChannel) {

    private lateinit var mediaController: MediaController

    private val token = SessionToken(
        context,
        ComponentName(context, PlaybackService::class.java),
    )

    private val handler = Handler(Looper.getMainLooper())

    init {
        val controllerBuilder = MediaController.Builder(context, token)
        val futureController = controllerBuilder.buildAsync()

        Futures.addCallback(
            futureController,
            object : FutureCallback<MediaController> {
                override fun onSuccess(controller: MediaController) {
                    mediaController = controller

                    LocalBroadcastManager
                        .getInstance(context)
                        .registerReceiver(bookDetailsReadyReceiver, IntentFilter(PLAYBACK_READY))

                    LocalBroadcastManager
                        .getInstance(context)
                        .registerReceiver(timerExpiredReceiver, IntentFilter(TIMER_EXPIRED))

                    mediaController.addListener(object : Player.Listener {
                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            _isPlaying.value = isPlaying
                        }

                        override fun onPlaybackStateChanged(playbackState: Int) {
                            if (playbackState == Player.STATE_ENDED) {
                                mediaController.seekTo(0, 0)
                                mediaController.pause()
                            }
                        }
                    })
                }

                override fun onFailure(t: Throwable) {
                    Napier.e("Unable to add callback to player")
                }
            },
            MoreExecutors.directExecutor(),
        )
    }

    private val bookDetailsReadyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == PLAYBACK_READY) {
                val jsonString = intent.getStringExtra(BOOK_EXTRA)
                val book = jsonString?.let { Json.decodeFromString<DetailedItem>(it) }

                book?.let {
                    CoroutineScope(Dispatchers.Main).launch {
                        updateProgress(book).await()
                        startUpdatingProgress(book)

                        _playingBook.value = it
                        _isPlaybackReady.value = true
                    }
                }
            }
        }
    }

    private val timerExpiredReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == TIMER_EXPIRED) {
                _timerOption.value = null
            }
        }
    }

    override fun setPlaybackSpeed(factor: Float) {
        val speed = when {
            factor < 0.5f -> 0.5f
            factor > 3f -> 3f
            else -> factor
        }

        if (::mediaController.isInitialized) {
            mediaController.setPlaybackSpeed(speed)
        }

        _playbackSpeed.value = speed
        preferences.savePlaybackSpeed(speed)
    }

    override fun scheduleServiceTimer(delay: Double) {
        val intent = Intent(context, PlaybackService::class.java).apply {
            action = PlaybackService.ACTION_SET_TIMER
            putExtra(TIMER_VALUE_EXTRA, delay)
        }

        context.startService(intent)
    }

    override fun cancelServiceTimer() {
        val intent = Intent(context, PlaybackService::class.java).apply {
            action = PlaybackService.ACTION_CANCEL_TIMER
        }

        context.startService(intent)
    }

    override fun startUpdatingProgress(detailedItem: DetailedItem) {
        handler.removeCallbacksAndMessages(null)

        handler.postDelayed(
            object : Runnable {
                override fun run() {
                    updateProgress(detailedItem)
                    handler.postDelayed(this, 500)
                }
            },
            500,
        )
    }

    override fun startPreparingPlayback(book: DetailedItem, fromBackground: Boolean) {
        if (::mediaController.isInitialized && _playingBook.value != book) {
            _totalPosition.value = 0.0
            _isPlaying.value = false

            val intent = Intent(context, PlaybackService::class.java).apply {
                action = PlaybackService.ACTION_SET_PLAYBACK
                // todo: DetailedItem is not a java.io.Serializable so we do a json serialization here
                //  this should be replaced in the future when the LocalBroadcastManager is replaced
                putExtra(BOOK_EXTRA, Json.encodeToString(book))
            }

            when (fromBackground) {
                true -> context.startForegroundService(intent)
                false -> context.startService(intent)
            }
        }
    }

    override fun updateProgress(book: DetailedItem): Deferred<Unit> {
        return CoroutineScope(Dispatchers.Main).async {
            val currentIndex = mediaController.currentMediaItemIndex
            val accumulated = book.files.take(currentIndex).sumOf { it.duration }
            val currentFilePosition = mediaController.currentPosition / 1000.0

            _totalPosition.value = (accumulated + currentFilePosition)
        }
    }

    override fun play() {
        val intent = Intent(context, PlaybackService::class.java).apply {
            action = PlaybackService.ACTION_PLAY
        }
        context.startForegroundService(intent)
    }

    override fun pause() {
        val intent = Intent(context, PlaybackService::class.java).apply {
            action = PlaybackService.ACTION_PAUSE
        }
        context.startService(intent)
    }

    override fun seekTo(position: Double) {
        val book = playingBook.value ?: return

        val overallDuration = book
            .chapters
            .sumOf { it.duration }

        val safePosition = minOf(overallDuration, maxOf(0.0, position))

        val intent = Intent(context, PlaybackService::class.java).apply {
            action = ACTION_SEEK_TO

            // todo: DetailedItem is not a java.io.Serializable so we do a json serialization here
            //  this should be replaced in the future when the LocalBroadcastManager is replaced
            putExtra(BOOK_EXTRA, Json.encodeToString(playingBook.value))
            putExtra(POSITION, safePosition)
        }

        context.startService(intent)

        when (_timerOption.value) {
            is CurrentEpisodeTimerOption -> updateTimer(
                timerOption = _timerOption.value,
                position = safePosition,
            )

            is DurationTimerOption -> Unit
            null -> Unit
        }
    }

}