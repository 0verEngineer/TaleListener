
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.common
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Changed provideFileUri to return Url
 */

package org.overengineer.talelistener.channel.common

import io.ktor.http.Url
import okio.BufferedSource
import org.overengineer.talelistener.domain.Book
import org.overengineer.talelistener.domain.DetailedItem
import org.overengineer.talelistener.domain.Library
import org.overengineer.talelistener.domain.PagedItems
import org.overengineer.talelistener.domain.PlaybackProgress
import org.overengineer.talelistener.domain.PlaybackSession
import org.overengineer.talelistener.domain.RecentBook

interface MediaChannel {

    fun getLibraryType(): LibraryType

    fun provideFileUrl(
        libraryItemId: String,
        fileId: String,
    ): Url

    suspend fun syncProgress(
        sessionId: String,
        progress: PlaybackProgress,
    ): ApiResult<Unit>

    suspend fun fetchBookCover(
        bookId: String,
    ): ApiResult<BufferedSource>

    suspend fun fetchBooks(
        libraryId: String,
        pageSize: Int,
        pageNumber: Int,
    ): ApiResult<PagedItems<Book>>

    suspend fun searchBooks(
        libraryId: String,
        query: String,
        limit: Int,
    ): ApiResult<List<Book>>

    suspend fun fetchLibraries(): ApiResult<List<Library>>

    suspend fun startPlayback(
        bookId: String,
        episodeId: String,
        supportedMimeTypes: List<String>,
        deviceId: String,
    ): ApiResult<PlaybackSession>

    suspend fun fetchConnectionInfo(): ApiResult<ConnectionInfo>

    suspend fun fetchRecentListenedBooks(libraryId: String): ApiResult<List<RecentBook>>

    suspend fun fetchBook(bookId: String): ApiResult<DetailedItem>
}
