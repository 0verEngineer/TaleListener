
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.audiobookshelf.common.api
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Removed javax singleton stuff
 */

package org.overengineer.talelistener.channel.audiobookshelf.common.api

import org.overengineer.talelistener.channel.audiobookshelf.common.client.AudiobookshelfApiClient
import org.overengineer.talelistener.channel.audiobookshelf.common.model.MediaProgressResponse
import org.overengineer.talelistener.channel.audiobookshelf.common.model.connection.ConnectionInfoResponse
import org.overengineer.talelistener.channel.audiobookshelf.common.model.metadata.AuthorItemsResponse
import org.overengineer.talelistener.channel.audiobookshelf.common.model.metadata.LibraryResponse
import org.overengineer.talelistener.channel.audiobookshelf.common.model.playback.PlaybackSessionResponse
import org.overengineer.talelistener.channel.audiobookshelf.common.model.playback.PlaybackStartRequest
import org.overengineer.talelistener.channel.audiobookshelf.common.model.playback.ProgressSyncRequest
import org.overengineer.talelistener.channel.audiobookshelf.common.model.user.PersonalizedFeedResponse
import org.overengineer.talelistener.channel.audiobookshelf.common.model.user.UserInfoResponse
import org.overengineer.talelistener.channel.audiobookshelf.library.model.BookResponse
import org.overengineer.talelistener.channel.audiobookshelf.library.model.LibraryItemsResponse
import org.overengineer.talelistener.channel.audiobookshelf.library.model.LibrarySearchResponse
import org.overengineer.talelistener.channel.audiobookshelf.podcast.model.PodcastItemsResponse
import org.overengineer.talelistener.channel.audiobookshelf.podcast.model.PodcastResponse
import org.overengineer.talelistener.channel.audiobookshelf.podcast.model.PodcastSearchResponse
import org.overengineer.talelistener.channel.common.ApiClient
import org.overengineer.talelistener.channel.common.ApiResult
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences
import org.overengineer.talelistener.platform.getHttpClientEngineFactory

// Todo: rename? - this is not a repository
class AudioBookshelfDataRepository (
    private val preferences: TaleListenerSharedPreferences
) {

    private var configCache: ApiClientConfig? = null
    private var clientCache: AudiobookshelfApiClient? = null

    suspend fun fetchLibraries(): ApiResult<LibraryResponse> =
        safeApiCall { getClientInstance().fetchLibraries() }

    suspend fun fetchAuthorItems(
        authorId: String,
    ): ApiResult<AuthorItemsResponse> = safeApiCall {
        getClientInstance()
            .fetchAuthorLibraryItems(
                authorId = authorId,
            )
    }

    suspend fun searchPodcasts(
        libraryId: String,
        query: String,
        limit: Int,
    ): ApiResult<PodcastSearchResponse> = safeApiCall {
        getClientInstance()
            .searchPodcasts(
                libraryId = libraryId,
                request = query,
                limit = limit,
            )
    }

    suspend fun searchBooks(
        libraryId: String,
        query: String,
        limit: Int,
    ): ApiResult<LibrarySearchResponse> = safeApiCall {
        getClientInstance()
            .searchLibraryItems(
                libraryId = libraryId,
                request = query,
                limit = limit,
            )
    }

    suspend fun fetchLibraryItems(
        libraryId: String,
        pageSize: Int,
        pageNumber: Int,
    ): ApiResult<LibraryItemsResponse> =
        safeApiCall {
            getClientInstance()
                .fetchLibraryItems(
                    libraryId = libraryId,
                    pageSize = pageSize,
                    pageNumber = pageNumber,
                )
        }

    suspend fun fetchPodcastItems(
        libraryId: String,
        pageSize: Int,
        pageNumber: Int,
    ): ApiResult<PodcastItemsResponse> =
        safeApiCall {
            getClientInstance()
                .fetchPodcastItems(
                    libraryId = libraryId,
                    pageSize = pageSize,
                    pageNumber = pageNumber,
                )
        }

    suspend fun fetchBook(itemId: String): ApiResult<BookResponse> =
        safeApiCall { getClientInstance().fetchLibraryItem(itemId) }

    suspend fun fetchPodcastItem(itemId: String): ApiResult<PodcastResponse> =
        safeApiCall { getClientInstance().fetchPodcastEpisode(itemId) }

    suspend fun fetchConnectionInfo(): ApiResult<ConnectionInfoResponse> =
        safeApiCall { getClientInstance().fetchConnectionInfo() }

    suspend fun fetchPersonalizedFeed(libraryId: String): ApiResult<List<PersonalizedFeedResponse>> =
        safeApiCall { getClientInstance().fetchPersonalizedFeed(libraryId) }

    suspend fun fetchLibraryItemProgress(itemId: String): ApiResult<MediaProgressResponse> =
        safeApiCall { getClientInstance().fetchLibraryItemProgress(itemId) }

    suspend fun fetchUserInfoResponse(): ApiResult<UserInfoResponse> =
        safeApiCall { getClientInstance().fetchUserInfo() }

    suspend fun startPlayback(
        itemId: String,
        request: PlaybackStartRequest,
    ): ApiResult<PlaybackSessionResponse> =
        safeApiCall { getClientInstance().startLibraryPlayback(itemId, request) }

    suspend fun startPodcastPlayback(
        itemId: String,
        episodeId: String,
        request: PlaybackStartRequest,
    ): ApiResult<PlaybackSessionResponse> =
        safeApiCall { getClientInstance().startPodcastPlayback(itemId, episodeId, request) }

    suspend fun stopPlayback(sessionId: String): ApiResult<Unit> =
        safeApiCall { getClientInstance().stopPlayback(sessionId) }

    suspend fun publishLibraryItemProgress(
        itemId: String,
        progress: ProgressSyncRequest,
    ): ApiResult<Unit> =
        safeApiCall { getClientInstance().publishLibraryItemProgress(itemId, progress) }

    private fun getClientInstance(): AudiobookshelfApiClient {
        val host = preferences.getHost()
        val token = preferences.getToken()

        val cache = ApiClientConfig(
            host = host,
            token = token,
            customHeaders = preferences.getCustomHeaders(),
        )

        val currentClientCache = clientCache

        return when (currentClientCache == null || cache != configCache) {
            true -> {
                val instance = createClientInstance()
                configCache = cache
                clientCache = instance
                instance
            }

            else -> currentClientCache
        }
    }

    private fun createClientInstance(): AudiobookshelfApiClient {
        val host = preferences.getHost()
        val token = preferences.getToken()

        if (host.isNullOrBlank() || token.isNullOrBlank()) {
            throw IllegalStateException("Host or token is missing")
        }

        val apiClient = ApiClient(
            serverUrlString = host,
            requestHeaders = preferences.getCustomHeaders(),
            token = token,
            engineFactory = getHttpClientEngineFactory()
        )

        return AudiobookshelfApiClient(apiClient.httpClient)
    }
}
