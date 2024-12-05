
package org.overengineer.talelistener.channel.audiobookshelf.common.api

import io.github.aakira.napier.Napier
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.errors.IOException
import org.overengineer.talelistener.channel.common.ApiError
import org.overengineer.talelistener.channel.common.ApiResult


suspend fun <T> safeApiCall(
    apiCall: suspend () -> T,
): ApiResult<T> {
    return try {
        val result = apiCall.invoke()
        ApiResult.Success(result)
    } catch (e: ClientRequestException) { // 4xx responses
        val response = e.response
        when (response.status) {
            HttpStatusCode.BadRequest -> ApiResult.Error(ApiError.InternalError)
            HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> ApiResult.Error(ApiError.Unauthorized)
            HttpStatusCode.NotFound -> ApiResult.Error(ApiError.InternalError)
            else -> ApiResult.Error(ApiError.InternalError)
        }
    } catch (e: ServerResponseException) { // 5xx responses
        Napier.e("Unable to make network api call $apiCall due to: $e")
        ApiResult.Error(ApiError.InternalError)
    } catch (e: IOException) {
        // Handle network I/O exceptions
        Napier.e("Unable to make network api call $apiCall due to: $e")
        ApiResult.Error(ApiError.NetworkError)
    } catch (e: Exception) {
        // Handle any other exceptions
        Napier.e("Unable to make network api call $apiCall due to: $e")
        ApiResult.Error(ApiError.InternalError)
    }
}
