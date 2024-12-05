
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.audiobookshelf.common.api
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Removed javax singleton stuff
 * - Changed to use TaleListenerSharedPreferences
 * - Replaced android specific stuff
 * - Changed to use TaleListeners BinaryApiClient
 * - Removed companion object with only TAG in it
 */

package org.overengineer.talelistener.channel.audiobookshelf.common.api

import io.github.aakira.napier.Napier
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.errors.IOException
import org.overengineer.talelistener.channel.audiobookshelf.common.client.AudiobookshelfMediaClient
import org.overengineer.talelistener.channel.common.ApiError
import org.overengineer.talelistener.channel.common.ApiResult
import org.overengineer.talelistener.channel.common.BinaryApiClient
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences
import org.overengineer.talelistener.platform.getHttpClientEngineFactory

class AudioBookshelfMediaRepository (
    private val preferences: TaleListenerSharedPreferences,
    private val requestHeadersProvider: RequestHeadersProvider,
) {

    private var configCache: ApiClientConfig? = null
    private var clientCache: AudiobookshelfMediaClient? = null

    // todo does this work?
    suspend fun fetchBookCover(itemId: String): ApiResult<ByteReadChannel> =
        safeCall { getClientInstance().getItemCover(itemId) }

    private suspend fun safeCall(apiCall: suspend () -> HttpResponse): ApiResult<ByteReadChannel> {
        return try {
            val response = apiCall.invoke()

            when (response.status.value) {
                200 -> {
                    val body = response.bodyAsChannel()
                    ApiResult.Success(body)
                }
                400 -> ApiResult.Error(ApiError.InternalError)
                401 -> ApiResult.Error(ApiError.Unauthorized)
                403 -> ApiResult.Error(ApiError.Unauthorized)
                404 -> ApiResult.Error(ApiError.InternalError)
                500 -> ApiResult.Error(ApiError.InternalError)
                else -> ApiResult.Error(ApiError.InternalError)
            }
        } catch (e: IOException) {
            Napier.e("Unable to make network api call $apiCall due to: $e")
            ApiResult.Error(ApiError.NetworkError)
        } catch (e: Exception) {
            Napier.e("Unable to make network api call $apiCall due to: $e")
            ApiResult.Error(ApiError.InternalError)
        }
    }


    // todo inspect this
    private fun getClientInstance(): AudiobookshelfMediaClient {
        val host = preferences.getHost()
        val token = preferences.getToken()

        val cache = ApiClientConfig(
            host = host,
            token = token,
            customHeaders = requestHeadersProvider.fetchRequestHeaders(),
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

    private fun createClientInstance(): AudiobookshelfMediaClient {
        val host = preferences.getHost()
        val token = preferences.getToken()

        if (host.isNullOrBlank() || token.isNullOrBlank()) {
            throw IllegalStateException("Host or token is missing")
        }

        val apiClient = BinaryApiClient(
            serverUrlString = host,
            requestHeaders = requestHeadersProvider.fetchRequestHeaders(),
            token = token,
            engineFactory = getHttpClientEngineFactory()
        )

        return AudiobookshelfMediaClient(apiClient.httpClient)
    }
}
