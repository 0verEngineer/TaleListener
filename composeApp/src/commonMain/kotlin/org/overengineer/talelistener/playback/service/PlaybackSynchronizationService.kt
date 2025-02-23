package org.overengineer.talelistener.playback.service

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancelChildren
import org.overengineer.talelistener.content.TLMediaProvider
import org.overengineer.talelistener.domain.DetailedItem
import org.overengineer.talelistener.domain.PlaybackProgress
import org.overengineer.talelistener.domain.PlaybackSession
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences

abstract class PlaybackSynchronizationService(
    private val mediaChannel: TLMediaProvider,
    private val sharedPreferences: TaleListenerSharedPreferences
) {

    protected var currentBook: DetailedItem? = null
    protected var currentChapterIndex: Int? = null
    protected var playbackSession: PlaybackSession? = null
    protected val serviceScope = MainScope()

    fun preparePlaybackSynchronization(book: DetailedItem) {
        serviceScope.coroutineContext.cancelChildren()
        currentBook = book
    }

    abstract fun scheduleSynchronization()
    abstract fun executeSynchronization()
    abstract fun getProgress(currentElapsedMs: Long): PlaybackProgress

    protected suspend fun synchronizeProgress(
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

    protected suspend fun openPlaybackSession(overallProgress: PlaybackProgress) = currentBook
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

    // todo: setting
    companion object {
        const val SYNC_INTERVAL = 15_000L
    }
}