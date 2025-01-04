
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
import io.ktor.http.contentLength
import io.ktor.utils.io.errors.IOException
import io.ktor.utils.io.readFully
import okio.Buffer
import okio.BufferedSource
import org.overengineer.talelistener.channel.audiobookshelf.common.client.AudiobookshelfMediaClient
import org.overengineer.talelistener.channel.common.ApiError
import org.overengineer.talelistener.channel.common.ApiResult
import org.overengineer.talelistener.channel.common.BinaryApiClient
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences
import org.overengineer.talelistener.platform.getHttpClientEngineFactory

// todo: rename? - this is not a repository
class AudioBookshelfMediaRepository (
    private val preferences: TaleListenerSharedPreferences
) {

    private var configCache: ApiClientConfig? = null
    private var clientCache: AudiobookshelfMediaClient? = null

    suspend fun fetchBookCover(itemId: String): ApiResult<BufferedSource> =
        safeCall { getClientInstance().getItemCover(itemId) }

    private suspend fun safeCall(apiCall: suspend () -> HttpResponse): ApiResult<BufferedSource> {
        return try {
            val response = apiCall.invoke()

            // todo now: is the async stuff here done correctly?
            when (response.status.value) {
                200 -> {
                    val byteArray = ByteArray(response.contentLength()!!.toInt())
                    val body = response.bodyAsChannel()
                    body.readFully(byteArray)
                    val buffer = Buffer()
                    buffer.write(byteArray)
                    ApiResult.Success(buffer)
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

    private fun createClientInstance(): AudiobookshelfMediaClient {
        val host = preferences.getHost()
        val token = preferences.getToken()

        if (host.isNullOrBlank() || token.isNullOrBlank()) {
            throw IllegalStateException("Host or token is missing")
        }

        val apiClient = BinaryApiClient(
            serverUrlString = host,
            requestHeaders = preferences.getCustomHeaders(),
            token = token,
            engineFactory = getHttpClientEngineFactory()
        )

        return AudiobookshelfMediaClient(apiClient.httpClient)
    }
}
