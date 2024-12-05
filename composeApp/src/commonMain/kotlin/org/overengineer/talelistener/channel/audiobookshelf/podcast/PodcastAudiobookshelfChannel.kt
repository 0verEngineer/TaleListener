
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.audiobookshelf.podcast
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Removed javax singleton stuff
 * - Use TaleListenerSharedPreferences
 */

package org.overengineer.talelistener.channel.audiobookshelf.podcast

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.overengineer.talelistener.channel.audiobookshelf.common.AudiobookshelfChannel
import org.overengineer.talelistener.channel.audiobookshelf.common.api.AudioBookshelfDataRepository
import org.overengineer.talelistener.channel.audiobookshelf.common.api.AudioBookshelfMediaRepository
import org.overengineer.talelistener.channel.audiobookshelf.common.api.AudioBookshelfSyncService
import org.overengineer.talelistener.channel.audiobookshelf.common.converter.ConnectionInfoResponseConverter
import org.overengineer.talelistener.channel.audiobookshelf.common.converter.LibraryResponseConverter
import org.overengineer.talelistener.channel.audiobookshelf.common.converter.PlaybackSessionResponseConverter
import org.overengineer.talelistener.channel.audiobookshelf.common.converter.RecentListeningResponseConverter
import org.overengineer.talelistener.channel.audiobookshelf.common.model.playback.DeviceInfo
import org.overengineer.talelistener.channel.audiobookshelf.common.model.playback.PlaybackStartRequest
import org.overengineer.talelistener.channel.audiobookshelf.podcast.converter.PodcastPageResponseConverter
import org.overengineer.talelistener.channel.audiobookshelf.podcast.converter.PodcastResponseConverter
import org.overengineer.talelistener.channel.audiobookshelf.podcast.converter.PodcastSearchItemsConverter
import org.overengineer.talelistener.channel.common.ApiResult
import org.overengineer.talelistener.channel.common.LibraryType
import org.overengineer.talelistener.domain.Book
import org.overengineer.talelistener.domain.DetailedItem
import org.overengineer.talelistener.domain.PagedItems
import org.overengineer.talelistener.domain.PlaybackSession
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences

class PodcastAudiobookshelfChannel (
    dataRepository: AudioBookshelfDataRepository,
    mediaRepository: AudioBookshelfMediaRepository,
    recentListeningResponseConverter: RecentListeningResponseConverter,
    preferences: TaleListenerSharedPreferences,
    syncService: AudioBookshelfSyncService,
    sessionResponseConverter: PlaybackSessionResponseConverter,
    libraryResponseConverter: LibraryResponseConverter,
    connectionInfoResponseConverter: ConnectionInfoResponseConverter,
    private val podcastPageResponseConverter: PodcastPageResponseConverter,
    private val podcastResponseConverter: PodcastResponseConverter,
    private val podcastSearchItemsConverter: PodcastSearchItemsConverter,
) : AudiobookshelfChannel(
    dataRepository = dataRepository,
    mediaRepository = mediaRepository,
    recentBookResponseConverter = recentListeningResponseConverter,
    sessionResponseConverter = sessionResponseConverter,
    preferences = preferences,
    syncService = syncService,
    libraryResponseConverter = libraryResponseConverter,
    connectionInfoResponseConverter = connectionInfoResponseConverter,
) {

    override fun getLibraryType() = LibraryType.PODCAST

    override suspend fun fetchBooks(
        libraryId: String,
        pageSize: Int,
        pageNumber: Int,
    ): ApiResult<PagedItems<Book>> = dataRepository
        .fetchPodcastItems(
            libraryId = libraryId,
            pageSize = pageSize,
            pageNumber = pageNumber,
        )
        .map { podcastPageResponseConverter.apply(it) }

    override suspend fun searchBooks(
        libraryId: String,
        query: String,
        limit: Int,
    ): ApiResult<List<Book>> = coroutineScope {
        val byTitle = async {
            dataRepository
                .searchPodcasts(libraryId, query, limit)
                .map { it.podcast }
                .map { it.map { response -> response.libraryItem } }
                .map { podcastSearchItemsConverter.apply(it) }
        }

        byTitle.await()
    }

    override suspend fun startPlayback(
        bookId: String,
        episodeId: String,
        supportedMimeTypes: List<String>,
        deviceId: String,
    ): ApiResult<PlaybackSession> {
        val request = PlaybackStartRequest(
            supportedMimeTypes = supportedMimeTypes,
            deviceInfo = DeviceInfo(
                clientName = getClientName(),
                deviceId = deviceId,
                deviceName = getClientName(),
            ),
            forceTranscode = false,
            forceDirectPlay = false,
            mediaPlayer = getClientName(),
        )

        return dataRepository
            .startPodcastPlayback(
                itemId = bookId,
                episodeId = episodeId,
                request = request,
            )
            .map { sessionResponseConverter.apply(it) }
    }

    override suspend fun fetchBook(bookId: String): ApiResult<DetailedItem> = coroutineScope {
        val mediaProgress = async {
            val progress = dataRepository
                .fetchUserInfoResponse()
                .fold(
                    onSuccess = { it.user.mediaProgress ?: emptyList() },
                    onFailure = { emptyList() },
                )

            if (progress.isEmpty()) {
                return@async null
            }

            progress
                .filter { it.libraryItemId == bookId }
                .maxByOrNull { it.lastUpdate }
        }

        async { dataRepository.fetchPodcastItem(bookId) }
            .await()
            .map { podcastResponseConverter.apply(it, mediaProgress.await()) }
    }
}
