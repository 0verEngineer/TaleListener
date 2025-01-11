
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.audiobookshelf.common.model.user
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Add Serializable annotations
 * - Fix media missing issue
 */

package org.overengineer.talelistener.channel.audiobookshelf.common.model.user

import kotlinx.serialization.Serializable

@Serializable
data class PersonalizedFeedResponse(
    val id: String,
    val labelStringKey: String,
    val entities: List<PersonalizedFeedItemResponse>,
)

@Serializable
data class PersonalizedFeedItemResponse(
    val id: String,
    val libraryId: String,
    val media: PersonalizedFeedItemMediaResponse? = null,
    val updatedAt: Long,
)

@Serializable
data class PersonalizedFeedItemMediaResponse(
    val id: String,
    val metadata: PersonalizedFeedItemMetadataResponse,
)

@Serializable
data class PersonalizedFeedItemMetadataResponse(
    val title: String,
    val authorName: String,
)
