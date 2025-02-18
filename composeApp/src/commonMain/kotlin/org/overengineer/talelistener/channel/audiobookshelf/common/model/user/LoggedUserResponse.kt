
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.audiobookshelf.common.model.user
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Update classes to serializable
 */

package org.overengineer.talelistener.channel.audiobookshelf.common.model.user

import kotlinx.serialization.Serializable

@Serializable
data class LoggedUserResponse(
    val user: User,
    val userDefaultLibraryId: String? = null,
)

@Serializable
data class User(
    val id: String,
    val token: String,
)
