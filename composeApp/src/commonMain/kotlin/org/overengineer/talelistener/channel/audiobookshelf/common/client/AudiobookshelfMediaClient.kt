package org.overengineer.talelistener.channel.audiobookshelf.common.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse


class AudiobookshelfMediaClient(private val httpClient: HttpClient) {

    suspend fun getItemCover(itemId: String): HttpResponse {
        return httpClient.get("/api/items/$itemId/cover").body()
    }
}
