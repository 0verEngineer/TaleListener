
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
data class LibraryItemsResponse(
    val results: List<LibraryItem>,
    val page: Int,
)

@Serializable
data class LibraryItem(
    val id: String,
    val media: Media,
)

@Serializable
data class Media(
    val duration: Double,
    val metadata: LibraryMetadata,
)

@Serializable
data class LibraryMetadata(
    val title: String?,
    val authorName: String?,
)
