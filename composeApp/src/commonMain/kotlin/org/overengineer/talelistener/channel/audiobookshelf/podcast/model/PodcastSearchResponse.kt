
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.audiobookshelf.podcast.model
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Add Serializable annotations
 */

package org.overengineer.talelistener.channel.audiobookshelf.podcast.model

import kotlinx.serialization.Serializable

@Serializable
data class PodcastSearchResponse(
    val podcast: List<PodcastSearchItemResponse>,
)

@Serializable
data class PodcastSearchItemResponse(
    val libraryItem: PodcastItem,
)
