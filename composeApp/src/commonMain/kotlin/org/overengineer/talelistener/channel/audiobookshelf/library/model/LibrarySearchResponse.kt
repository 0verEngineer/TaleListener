
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.audiobookshelf.library.model
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Add Serializable annotations
 */

package org.overengineer.talelistener.channel.audiobookshelf.library.model

import kotlinx.serialization.Serializable

@Serializable
data class LibrarySearchResponse(
    val book: List<LibrarySearchItemResponse>,
    val authors: List<LibrarySearchAuthorResponse>,
)

@Serializable
data class LibrarySearchItemResponse(
    val libraryItem: LibraryItem,
)

@Serializable
data class LibrarySearchAuthorResponse(
    val id: String,
    val name: String,
)
