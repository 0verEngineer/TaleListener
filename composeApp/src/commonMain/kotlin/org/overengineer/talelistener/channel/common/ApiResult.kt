
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.common
 * Modifications: Updated package statement and adjusted imports.
 */

package org.overengineer.talelistener.channel.common

sealed class ApiResult<T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error<T>(val code: ApiError, val message: String? = null) : ApiResult<T>()

    fun <R> fold(
        onSuccess: (T) -> R,
        onFailure: (Error<T>) -> R,
    ): R {
        return when (this) {
            is Success -> onSuccess(this.data)
            is Error -> onFailure(this)
        }
    }

    suspend fun <R> foldAsync(
        onSuccess: suspend (T) -> R,
        onFailure: suspend (Error<T>) -> R,
    ): R {
        return when (this) {
            is Success -> onSuccess(this.data)
            is Error -> onFailure(this)
        }
    }

    suspend fun <R> map(transform: suspend (T) -> R): ApiResult<R> {
        return when (this) {
            is Success -> Success(transform(this.data))
            is Error -> Error(this.code, this.message)
        }
    }

    suspend fun <R> flatMap(transform: suspend (T) -> ApiResult<R>): ApiResult<R> {
        return when (this) {
            is Success -> transform(this.data)
            is Error -> Error(this.code, this.message)
        }
    }
}
