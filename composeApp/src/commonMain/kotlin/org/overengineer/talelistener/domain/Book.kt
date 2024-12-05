
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.domain
 * Modifications:
 * - Updated package statement and adjusted imports.
 */

package org.overengineer.talelistener.domain

data class Book(
    val id: String,
    val title: String,
    val author: String?,
    val duration: Int,
    val cachedState: BookCachedState,
)

enum class BookCachedState {
    ABLE_TO_CACHE,
    UNABLE_TO_CACHE,
    CACHED,
}
