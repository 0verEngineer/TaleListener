
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.audiobookshelf.common.converter
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Removed javax singleton stuff
 */

package org.overengineer.talelistener.channel.audiobookshelf.common.converter

import org.overengineer.talelistener.channel.audiobookshelf.common.model.metadata.LibraryResponse
import org.overengineer.talelistener.channel.common.LibraryType
import org.overengineer.talelistener.domain.Library

class LibraryResponseConverter constructor() {

    fun apply(response: LibraryResponse): List<Library> = response
        .libraries
        .map {
            it
                .mediaType
                .toLibraryType()
                .let { type -> Library(it.id, it.name, type) }
        }

    private fun String.toLibraryType() = when (this) {
        "podcast" -> LibraryType.PODCAST
        "book" -> LibraryType.LIBRARY
        else -> LibraryType.UNKNOWN
    }
}
