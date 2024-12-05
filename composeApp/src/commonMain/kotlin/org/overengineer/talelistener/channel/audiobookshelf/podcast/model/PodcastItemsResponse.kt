
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
data class PodcastItemsResponse(
    val results: List<PodcastItem>,
    val page: Int,
)

@Serializable
data class PodcastItem(
    val id: String,
    val media: PodcastItemMedia,
)

@Serializable
data class PodcastItemMedia(
    val duration: Double,
    val metadata: PodcastMetadata,
)

@Serializable
data class PodcastMetadata(
    val title: String?,
    val author: String?,
)
