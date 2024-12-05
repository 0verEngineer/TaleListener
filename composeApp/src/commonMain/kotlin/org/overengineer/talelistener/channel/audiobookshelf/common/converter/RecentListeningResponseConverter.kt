
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.audiobookshelf.common.converter
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Removed javax singleton stuff
 */

package org.overengineer.talelistener.channel.audiobookshelf.common.converter

import org.overengineer.talelistener.channel.audiobookshelf.common.model.user.PersonalizedFeedResponse
import org.overengineer.talelistener.domain.RecentBook

class RecentListeningResponseConverter constructor() {

    fun apply(
        response: List<PersonalizedFeedResponse>,
        progress: Map<String, Double>,
    ): List<RecentBook> = response
        .find { it.labelStringKey == LABEL_CONTINUE_LISTENING }
        ?.entities
        ?.distinctBy { it.id }
        ?.map {
            RecentBook(
                id = it.id,
                title = it.media.metadata.title,
                author = it.media.metadata.authorName,
                listenedPercentage = progress[it.id]?.let { it * 100 }?.toInt(),
            )
        } ?: emptyList()

    companion object {

        private const val LABEL_CONTINUE_LISTENING = "LabelContinueListening"
    }
}
