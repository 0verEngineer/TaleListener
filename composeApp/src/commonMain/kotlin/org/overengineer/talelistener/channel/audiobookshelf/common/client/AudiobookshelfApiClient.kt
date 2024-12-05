package org.overengineer.talelistener.channel.audiobookshelf.common.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.overengineer.talelistener.channel.audiobookshelf.common.model.MediaProgressResponse
import org.overengineer.talelistener.channel.audiobookshelf.common.model.connection.ConnectionInfoResponse
import org.overengineer.talelistener.channel.audiobookshelf.common.model.metadata.AuthorItemsResponse
import org.overengineer.talelistener.channel.audiobookshelf.common.model.metadata.LibraryResponse
import org.overengineer.talelistener.channel.audiobookshelf.common.model.playback.PlaybackSessionResponse
import org.overengineer.talelistener.channel.audiobookshelf.common.model.playback.PlaybackStartRequest
import org.overengineer.talelistener.channel.audiobookshelf.common.model.playback.ProgressSyncRequest
import org.overengineer.talelistener.channel.audiobookshelf.common.model.user.CredentialsLoginRequest
import org.overengineer.talelistener.channel.audiobookshelf.common.model.user.LoggedUserResponse
import org.overengineer.talelistener.channel.audiobookshelf.common.model.user.PersonalizedFeedResponse
import org.overengineer.talelistener.channel.audiobookshelf.common.model.user.UserInfoResponse
import org.overengineer.talelistener.channel.audiobookshelf.library.model.BookResponse
import org.overengineer.talelistener.channel.audiobookshelf.library.model.LibraryItemsResponse
import org.overengineer.talelistener.channel.audiobookshelf.library.model.LibrarySearchResponse
import org.overengineer.talelistener.channel.audiobookshelf.podcast.model.PodcastItemsResponse
import org.overengineer.talelistener.channel.audiobookshelf.podcast.model.PodcastResponse
import org.overengineer.talelistener.channel.audiobookshelf.podcast.model.PodcastSearchResponse


class AudiobookshelfApiClient(private val httpClient: HttpClient) {

    suspend fun fetchLibraries(): LibraryResponse =
        httpClient.get("/api/libraries").body()

    suspend fun fetchPersonalizedFeed(libraryId: String): List<PersonalizedFeedResponse> =
        httpClient.get("/api/libraries/$libraryId/personalized").body()

    suspend fun fetchLibraryItemProgress(itemId: String): MediaProgressResponse =
        httpClient.get("/api/me/progress/$itemId").body()

    suspend fun fetchConnectionInfo(): ConnectionInfoResponse =
        httpClient.post("/api/authorize").body()

    suspend fun fetchUserInfo(): UserInfoResponse =
        httpClient.post("/api/authorize").body()

    suspend fun fetchLibraryItems(
        libraryId: String,
        pageSize: Int,
        pageNumber: Int
    ): LibraryItemsResponse =
        httpClient.get("/api/libraries/$libraryId/items") {
            parameter("sort", "media.metadata.title")
            parameter("minified", "1")
            parameter("limit", pageSize)
            parameter("page", pageNumber)
        }.body()

    suspend fun fetchPodcastItems(
        libraryId: String,
        pageSize: Int,
        pageNumber: Int
    ): PodcastItemsResponse =
        httpClient.get("/api/libraries/$libraryId/items") {
            parameter("sort", "mtimeMs")
            parameter("desc", "1")
            parameter("limit", pageSize)
            parameter("page", pageNumber)
        }.body()

    suspend fun searchLibraryItems(libraryId: String, request: String, limit: Int): LibrarySearchResponse =
        httpClient.get("/api/libraries/$libraryId/search") {
            parameter("q", request)
            parameter("limit", limit)
        }.body()

    suspend fun searchPodcasts(libraryId: String, request: String, limit: Int): PodcastSearchResponse =
        httpClient.get("/api/libraries/$libraryId/search") {
            parameter("q", request)
            parameter("limit", limit)
        }.body()

    suspend fun fetchLibraryItem(itemId: String): BookResponse =
        httpClient.get("/api/items/$itemId").body()

    suspend fun fetchPodcastEpisode(itemId: String): PodcastResponse =
        httpClient.get("/api/items/$itemId").body()

    suspend fun fetchAuthorLibraryItems(authorId: String): AuthorItemsResponse =
        httpClient.get("/api/authors/$authorId") {
            parameter("include", "items")
        }.body()

    suspend fun publishLibraryItemProgress(itemId: String, syncProgressRequest: ProgressSyncRequest) {
        httpClient.post("/api/session/$itemId/sync") {
            contentType(ContentType.Application.Json)
            setBody(syncProgressRequest)
        }
    }

    suspend fun startPodcastPlayback(
        itemId: String,
        episodeId: String,
        syncProgressRequest: PlaybackStartRequest
    ): PlaybackSessionResponse =
        httpClient.post("/api/items/$itemId/play/$episodeId") {
            contentType(ContentType.Application.Json)
            setBody(syncProgressRequest)
        }.body()

    suspend fun startLibraryPlayback(
        itemId: String,
        syncProgressRequest: PlaybackStartRequest
    ): PlaybackSessionResponse =
        httpClient.post("/api/items/$itemId/play") {
            contentType(ContentType.Application.Json)
            setBody(syncProgressRequest)
        }.body()

    suspend fun stopPlayback(sessionId: String) {
        httpClient.post("/api/session/$sessionId/close")
    }

    suspend fun login(request: CredentialsLoginRequest): LoggedUserResponse =
        httpClient.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}
