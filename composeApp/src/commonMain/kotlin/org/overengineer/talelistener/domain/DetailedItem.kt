
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.common
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Update Serializable to kotlinx.serialization
 */

package org.overengineer.talelistener.domain

import kotlinx.serialization.Serializable


@Serializable
data class DetailedItem(
    val id: String,
    val title: String,
    val author: String?,
    val files: List<BookFile>,
    val chapters: List<BookChapter>,
    val progress: MediaProgress?,
    val libraryId: String?,
)

@Serializable
data class BookFile(
    val id: String,
    val name: String,
    val duration: Double,
    val mimeType: String,
)

@Serializable
data class MediaProgress(
    val currentTime: Double,
    val isFinished: Boolean,
    val lastUpdate: Long,
)

@Serializable
data class BookChapter(
    val duration: Double,
    val start: Double,
    val end: Double,
    val title: String,
    val id: String,
)
