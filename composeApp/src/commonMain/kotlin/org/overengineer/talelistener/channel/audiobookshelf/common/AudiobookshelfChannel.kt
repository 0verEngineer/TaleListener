
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.audiobookshelf.common
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Change fetchBookCover to match kotlin multiplatform changes
 * - Change getClientName to use TaleListener
 * - Change provideFileUri to match kotlin multiplatform changes
 */

package org.overengineer.talelistener.channel.audiobookshelf.common

import io.ktor.http.URLBuilder
import io.ktor.utils.io.ByteReadChannel
import io.ktor.http.Url
import io.ktor.http.path
import org.overengineer.talelistener.channel.audiobookshelf.common.api.AudioBookshelfDataRepository
import org.overengineer.talelistener.channel.audiobookshelf.common.api.AudioBookshelfMediaRepository
import org.overengineer.talelistener.channel.audiobookshelf.common.api.AudioBookshelfSyncService
import org.overengineer.talelistener.channel.audiobookshelf.common.converter.ConnectionInfoResponseConverter
import org.overengineer.talelistener.channel.audiobookshelf.common.converter.LibraryResponseConverter
import org.overengineer.talelistener.channel.audiobookshelf.common.converter.PlaybackSessionResponseConverter
import org.overengineer.talelistener.channel.audiobookshelf.common.converter.RecentListeningResponseConverter
import org.overengineer.talelistener.channel.common.ApiResult
import org.overengineer.talelistener.channel.common.ConnectionInfo
import org.overengineer.talelistener.channel.common.MediaChannel
import org.overengineer.talelistener.domain.Library
import org.overengineer.talelistener.domain.PlaybackProgress
import org.overengineer.talelistener.domain.RecentBook
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences

abstract class AudiobookshelfChannel(
    protected val dataRepository: AudioBookshelfDataRepository,
    protected val sessionResponseConverter: PlaybackSessionResponseConverter,
    protected val preferences: TaleListenerSharedPreferences,
    private val syncService: AudioBookshelfSyncService,
    private val libraryResponseConverter: LibraryResponseConverter,
    private val mediaRepository: AudioBookshelfMediaRepository,
    private val recentBookResponseConverter: RecentListeningResponseConverter,
    private val connectionInfoResponseConverter: ConnectionInfoResponseConverter,
) : MediaChannel {

    override fun provideFileUri(
        libraryItemId: String,
        fileId: String
    ): Url {
        val host = preferences.getHost() ?: throw IllegalStateException("Host is missing")
        val token = preferences.getToken() ?: throw IllegalStateException("Token is missing")

        return URLBuilder(host)
            .apply {
                path("api", "items", libraryItemId, "file", fileId)
                parameters.append("token", token)
            }.build()
    }

    override suspend fun syncProgress(
        sessionId: String,
        progress: PlaybackProgress,
    ): ApiResult<Unit> = syncService.syncProgress(sessionId, progress)

    override suspend fun fetchBookCover(
        bookId: String,
    ): ApiResult<ByteReadChannel> = mediaRepository.fetchBookCover(bookId)

    override suspend fun fetchLibraries(): ApiResult<List<Library>> = dataRepository
        .fetchLibraries()
        .map { libraryResponseConverter.apply(it) }

    override suspend fun fetchRecentListenedBooks(libraryId: String): ApiResult<List<RecentBook>> {
        val progress: Map<String, Double> = dataRepository
            .fetchUserInfoResponse()
            .fold(
                onSuccess = {
                    it
                        .user
                        .mediaProgress
                        ?.associate { item -> item.libraryItemId to item.progress }
                        ?: emptyMap()
                },
                onFailure = { emptyMap() },
            )

        return dataRepository
            .fetchPersonalizedFeed(libraryId)
            .map { recentBookResponseConverter.apply(it, progress) }
    }

    override suspend fun fetchConnectionInfo(): ApiResult<ConnectionInfo> = dataRepository
        .fetchConnectionInfo()
        .map { connectionInfoResponseConverter.apply(it) }

    // TODO: Add build config support instead of hardcoding
    protected fun getClientName() = "TaleListener App 0.0.1"
}
