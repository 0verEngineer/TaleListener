
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.audiobookshelf.common.api
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Removed javax singleton stuff
 * - ApiClient constructor modified to match TaleListeners ApiClient
 * - Creation of AudiobookshelfApiClient modified to match TaleListeners AudiobookshelfApiClient
 */

package org.overengineer.talelistener.channel.audiobookshelf.common.api

import org.overengineer.talelistener.channel.audiobookshelf.common.client.AudiobookshelfApiClient
import org.overengineer.talelistener.channel.audiobookshelf.common.model.user.CredentialsLoginRequest
import org.overengineer.talelistener.channel.audiobookshelf.common.model.user.LoggedUserResponse
import org.overengineer.talelistener.channel.common.ApiClient
import org.overengineer.talelistener.channel.common.ApiError
import org.overengineer.talelistener.channel.common.ApiResult
import org.overengineer.talelistener.channel.common.AuthType
import org.overengineer.talelistener.channel.common.ChannelAuthService
import org.overengineer.talelistener.content.cache.converter.loggedUserResponseToUserAccount
import org.overengineer.talelistener.domain.UserAccount
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences
import org.overengineer.talelistener.platform.getHttpClientEngineFactory

class AudiobookshelfAuthService (
    private val preferences: TaleListenerSharedPreferences,
) : ChannelAuthService {

    override suspend fun authorize(
        host: String,
        username: String,
        password: String,
    ): ApiResult<UserAccount> {
        if (host.isBlank() || !urlPattern.matches(host)) {
            return ApiResult.Error(ApiError.InvalidCredentialsHost)
        }

        lateinit var apiService: AudiobookshelfApiClient

        try {
            val apiClient = ApiClient(
                serverUrlString = host,
                requestHeaders = preferences.getCustomHeaders(),
                engineFactory = getHttpClientEngineFactory()
            )

            apiService = AudiobookshelfApiClient(apiClient.httpClient)
        } catch (e: Exception) {
            return ApiResult.Error(ApiError.InternalError)
        }

        val response: ApiResult<LoggedUserResponse> =
            safeApiCall { apiService.login(CredentialsLoginRequest(username, password)) }

        return response
            .fold(
                onSuccess = {
                    ApiResult.Success(loggedUserResponseToUserAccount(it))
                },
                onFailure = { ApiResult.Error(it.code) },
            )
    }

    override fun getAuthType(): AuthType = AuthType.CREDENTIALS

    private companion object {
        val urlPattern = Regex("^(http|https)://.*\$")
    }
}
