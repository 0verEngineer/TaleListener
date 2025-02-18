
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
data class BookResponse(
    val id: String,
    val ino: String,
    val libraryId: String,
    val media: BookMedia,
)

@Serializable
data class BookMedia(
    val metadata: LibraryMetadataResponse,
    val audioFiles: List<BookAudioFileResponse>? = null,
    val chapters: List<LibraryChapterResponse>? = null,
)

@Serializable
data class LibraryMetadataResponse(
    val title: String,
    val authors: List<LibraryAuthorResponse>? = null,
)

@Serializable
data class LibraryAuthorResponse(
    val id: String,
    val name: String,
)

@Serializable
data class BookAudioFileResponse(
    val index: Int,
    val ino: String,
    val duration: Double,
    val metadata: AudioFileMetadata,
    val metaTags: AudioFileTag? = null,
    val mimeType: String,
)

@Serializable
data class AudioFileMetadata(
    val filename: String,
    val ext: String,
    val size: Long,
)

@Serializable
data class AudioFileTag(
    val tagAlbum: String? = null,
    val tagTitle: String? = null,
)

@Serializable
data class LibraryChapterResponse(
    val start: Double,
    val end: Double,
    val title: String,
    val id: String,
)
