
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
import io.ktor.http.Url
import io.ktor.http.path
import okio.BufferedSource
import org.overengineer.talelistener.BuildConfig
import org.overengineer.talelistener.channel.audiobookshelf.common.api.AudioBookshelfDataRepository
import org.overengineer.talelistener.channel.audiobookshelf.common.api.AudioBookshelfMediaRepository
import org.overengineer.talelistener.channel.audiobookshelf.common.api.AudioBookshelfSyncService
import org.overengineer.talelistener.channel.common.ApiResult
import org.overengineer.talelistener.channel.common.ConnectionInfo
import org.overengineer.talelistener.channel.common.MediaChannel
import org.overengineer.talelistener.content.cache.converter.connectionInfoResponseToConnectionInfo
import org.overengineer.talelistener.content.cache.converter.libraryResponseToLibraryList
import org.overengineer.talelistener.content.cache.converter.personalizedFeedResponseAndProgressToRecentBookList
import org.overengineer.talelistener.domain.Library
import org.overengineer.talelistener.domain.PlaybackProgress
import org.overengineer.talelistener.domain.RecentBook
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences

abstract class AudiobookshelfChannel(
    protected val dataRepository: AudioBookshelfDataRepository,
    protected val preferences: TaleListenerSharedPreferences,
    private val syncService: AudioBookshelfSyncService,
    private val mediaRepository: AudioBookshelfMediaRepository,
) : MediaChannel {

    override fun provideFileUrl(
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
    ): ApiResult<BufferedSource> = mediaRepository.fetchBookCover(bookId)

    override suspend fun fetchLibraries(): ApiResult<List<Library>> = dataRepository
        .fetchLibraries()
        .map { libraryResponseToLibraryList(it) }

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
            .map { personalizedFeedResponseAndProgressToRecentBookList(it, progress) }
    }

    override suspend fun fetchConnectionInfo(): ApiResult<ConnectionInfo> = dataRepository
        .fetchConnectionInfo()
        .map { connectionInfoResponseToConnectionInfo(it) }

    protected fun getClientName(): String {
        return "${BuildConfig.appName} ${BuildConfig.version}"
    }
}
