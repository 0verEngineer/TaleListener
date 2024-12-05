
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.audiobookshelf.common.model.metadata
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Add Serializable annotations
 */

package org.overengineer.talelistener.channel.audiobookshelf.common.model.metadata

import kotlinx.serialization.Serializable
import org.overengineer.talelistener.channel.audiobookshelf.library.model.LibraryItem

@Serializable
data class AuthorItemsResponse(
    val libraryItems: List<LibraryItem>,
)
