
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
data class PodcastResponse(
    val id: String,
    val ino: String,
    val libraryId: String,
    val media: PodcastMedia,
)

@Serializable
data class PodcastMedia(
    val metadata: PodcastMediaMetadataResponse,
    val episodes: List<PodcastEpisodeResponse>? = null,
)

@Serializable
data class PodcastMediaMetadataResponse(
    val title: String,
    val author: String? = null,
)

@Serializable
data class PodcastEpisodeResponse(
    val id: String,
    val season: String? = null,
    val episode: String? = null,
    val pubDate: String? = null,
    val title: String,
    val audioFile: PodcastAudioFileResponse,
)

@Serializable
data class PodcastAudioFileResponse(
    val index: Int,
    val ino: String,
    val duration: Double,
    val mimeType: String,
)
