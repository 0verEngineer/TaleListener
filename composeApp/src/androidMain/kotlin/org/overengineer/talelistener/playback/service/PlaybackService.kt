
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.playback.service
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Use koin di
 * - Changed the action names
 * - Logic changes to match TaleListener logic
 */


package org.overengineer.talelistener.playback.service
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.koin.android.ext.android.inject
import org.overengineer.talelistener.content.TLMediaProvider
import org.overengineer.talelistener.domain.BookFile
import org.overengineer.talelistener.domain.DetailedItem
import org.overengineer.talelistener.domain.MediaProgress
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences
import org.overengineer.talelistener.platform.withTrustedCertificates


class PlaybackService : MediaSessionService() {

    private val exoPlayer: ExoPlayer by inject()
    private val mediaSession: MediaSession by inject()
    private val mediaChannel: TLMediaProvider by inject()
    private val playbackSynchronizationService: PlaybackSynchronizationServiceAndroid by inject()
    private val sharedPreferences: TaleListenerSharedPreferences by inject()

    private val playerServiceScope = MainScope()

    private val handler = Handler(Looper.getMainLooper())

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_SET_TIMER -> {
                val delay = intent.getDoubleExtra(TIMER_VALUE_EXTRA, 0.0)

                if (delay > 0) {
                    setTimer(delay)
                }

                return START_NOT_STICKY
            }

            ACTION_CANCEL_TIMER -> {
                cancelTimer()
                return START_NOT_STICKY
            }

            ACTION_PLAY -> {
                playerServiceScope
                    .launch {
                        exoPlayer.prepare()
                        exoPlayer.setPlaybackSpeed(sharedPreferences.getPlaybackSpeed())
                        exoPlayer.playWhenReady = true
                    }
                return START_STICKY
            }

            ACTION_PAUSE -> {
                pause()
                return START_NOT_STICKY
            }

            ACTION_SET_PLAYBACK -> {
                val jsonString = intent.getStringExtra(BOOK_EXTRA)
                val book = jsonString?.let { Json.decodeFromString<DetailedItem>(it) }
                if (book != null) {
                    playerServiceScope
                        .launch { preparePlayback(book) }
                }
                return START_NOT_STICKY
            }

            ACTION_SEEK_TO -> {
                val jsonString = intent.getStringExtra(BOOK_EXTRA)
                val book = jsonString?.let { Json.decodeFromString<DetailedItem>(it) }
                val position = intent.getDoubleExtra(POSITION, 0.0)
                book?.let { seek(it.files, position) }
                return START_NOT_STICKY
            }

            else -> {
                return START_NOT_STICKY
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession

    override fun onDestroy() {
        playerServiceScope.cancel()

        mediaSession.release()
        exoPlayer.release()
        exoPlayer.clearMediaItems()

        super.onDestroy()
    }

    @OptIn(UnstableApi::class)
    private suspend fun preparePlayback(book: DetailedItem) {
        exoPlayer.playWhenReady = false

        withContext(Dispatchers.IO) {
            val prepareQueue = async {

                // todo: this is the reason lissen loads and caches the cover on its own, we need it here for widgets etc, how to do it?
                /*val cover: ByteArray? = mediaChannel
                    .fetchBookCover(bookId = book.id)
                    .fold(
                        onSuccess = {
                            try {
                                it.readBytes()
                            } catch (ex: Exception) {
                                null
                            }
                        },
                        onFailure = { null },
                    )*/

                val sourceFactory = buildDataSourceFactory()

                val playingQueue = book
                    .files
                    .map { file ->
                        val url = mediaChannel.provideFileUrl(book.id, file.id)  // todo: local files...
                        val mediaData = MediaMetadata.Builder()
                            .setTitle(file.name)
                            .setArtist(book.title)

                        //cover?.let { mediaData.setArtworkData(it, PICTURE_TYPE_FRONT_COVER) }

                        val mediaItem = MediaItem.Builder()
                            .setMediaId(file.id)
                            .setUri(url.toString())
                            .setTag(book)
                            .setMediaMetadata(mediaData.build())
                            .build()

                        ProgressiveMediaSource
                            .Factory(sourceFactory)
                            .createMediaSource(mediaItem)
                    }

                withContext(Dispatchers.Main) {
                    exoPlayer.setMediaSources(playingQueue)
                    setPlaybackProgress(book.files, book.progress)
                }
            }

            val prepareSession = async {
                playbackSynchronizationService.preparePlaybackSynchronization(book)
            }

            awaitAll(prepareSession, prepareQueue)

            // todo: DetailedItem is not a java.io.Serializable so we do a json serialization here
            //  this should be replaced in the future when the LocalBroadcastManager is replaced
            val intent = Intent(PLAYBACK_READY).apply {
                putExtra(BOOK_EXTRA, Json.encodeToString(book))
            }

            LocalBroadcastManager
                .getInstance(baseContext)
                .sendBroadcast(intent)
        }
    }

    private fun setTimer(delay: Double) {
        val delayMs = delay * 1000

        cancelTimer()

        handler.postDelayed(
            {
                pause()
                LocalBroadcastManager
                    .getInstance(baseContext)
                    .sendBroadcast(Intent(TIMER_EXPIRED))
            },
            delayMs.toLong(),
        )
        Log.d(TAG, "Timer started for $delayMs ms.")
    }

    private fun cancelTimer() {
        handler.removeCallbacksAndMessages(null)
        Log.d(TAG, "Timer canceled.")
    }

    private fun pause() {
        playerServiceScope
            .launch {
                exoPlayer.playWhenReady = false
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
    }

    private fun seek(
        items: List<BookFile>,
        position: Double?,
    ) {
        if (items.isEmpty()) {
            Log.w(TAG, "Tried to seek position $position in the empty book. Skipping")
            return
        }

        when (position) {
            null -> exoPlayer.seekTo(0, 0)
            else -> {
                val positionMs = (position * 1000).toLong()

                val durationsMs = items.map { (it.duration * 1000).toLong() }
                val cumulativeDurationsMs = durationsMs.runningFold(0L) { acc, duration -> acc + duration }

                val targetChapterIndex = cumulativeDurationsMs.indexOfFirst { it > positionMs }

                if (targetChapterIndex == -1) {
                    val lastChapterIndex = items.size - 1
                    val lastChapterDurationMs = durationsMs.last()
                    exoPlayer.seekTo(lastChapterIndex, lastChapterDurationMs)
                    return
                }

                val chapterStartTimeMs = cumulativeDurationsMs[targetChapterIndex - 1]
                val chapterProgressMs = positionMs - chapterStartTimeMs
                exoPlayer.seekTo(targetChapterIndex - 1, chapterProgressMs)
            }
        }
    }

    private fun setPlaybackProgress(
        chapters: List<BookFile>,
        progress: MediaProgress?,
    ) = seek(chapters, progress?.currentTime)

    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient
            .Builder()
            .withTrustedCertificates()
            .build()
    }

    @OptIn(UnstableApi::class)
    private fun buildDataSourceFactory(): DefaultDataSource.Factory {
        val requestHeaders = sharedPreferences.getCustomHeaders()
            .associate { it.name to it.value }

        val okHttpClient = createOkHttpClient()

        val okHttpDataSourceFactory = OkHttpDataSource
            .Factory(okHttpClient)
            .setDefaultRequestProperties(requestHeaders)

        return DefaultDataSource.Factory(
            baseContext,
            okHttpDataSourceFactory,
        )
    }

    companion object {

        const val ACTION_PLAY = "org.overengineer.talelistener.player.service.PLAY"
        const val ACTION_PAUSE = "org.overengineer.talelistener.player.service.PAUSE"
        const val ACTION_SET_PLAYBACK = "org.overengineer.talelistener.player.service.SET_PLAYBACK"
        const val ACTION_SEEK_TO = "org.overengineer.talelistener.player.service.ACTION_SEEK_TO"
        const val ACTION_SET_TIMER = "org.overengineer.talelistener.player.service.ACTION_SET_TIMER"
        const val ACTION_CANCEL_TIMER = "org.overengineer.talelistener.player.service.CANCEL_TIMER"

        const val BOOK_EXTRA = "org.overengineer.talelistener.player.service.BOOK"
        const val TIMER_VALUE_EXTRA = "org.overengineer.talelistener.player.service.TIMER_VALUE"
        const val TIMER_EXPIRED = "org.overengineer.talelistener.player.service.TIMER_EXPIRED"

        const val PLAYBACK_READY = "org.overengineer.talelistener.player.service.PLAYBACK_READY"
        const val POSITION = "org.overengineer.talelistener.player.service.POSITION"

        private const val TAG: String = "PlaybackService"
    }
}
