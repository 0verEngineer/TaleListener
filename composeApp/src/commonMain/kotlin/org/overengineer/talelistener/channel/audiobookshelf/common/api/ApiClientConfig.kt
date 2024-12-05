/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.audiobookshelf.common.api
 * Modifications: Updated package statement and adjusted imports.
 */

package org.overengineer.talelistener.channel.audiobookshelf.common.api

import org.overengineer.talelistener.domain.connection.ServerRequestHeader

data class ApiClientConfig(
    val host: String?,
    val token: String?,
    val customHeaders: List<ServerRequestHeader>?,
)
