
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.audiobookshelf.common.model
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Add Serializable annotations
 */

package org.overengineer.talelistener.channel.audiobookshelf.common.model

import kotlinx.serialization.Serializable

@Serializable
data class MediaProgressResponse(
    val libraryItemId: String,
    val episodeId: String? = null,
    val currentTime: Double,
    val isFinished: Boolean,
    val lastUpdate: Long,
    val progress: Double,
)
