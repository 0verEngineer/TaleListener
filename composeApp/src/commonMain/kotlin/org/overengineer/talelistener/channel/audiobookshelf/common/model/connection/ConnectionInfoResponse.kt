
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.audiobookshelf.common.model.connection
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Add Serializable annotations
 */

package org.overengineer.talelistener.channel.audiobookshelf.common.model.connection

import kotlinx.serialization.Serializable

@Serializable
data class ConnectionInfoResponse(
    val user: ConnectionInfoUserResponse,
    val serverSettings: ConnectionInfoServerResponse? = null,
)

@Serializable
data class ConnectionInfoUserResponse(
    val username: String
)

@Serializable
data class ConnectionInfoServerResponse(
    val version: String? = null,
    val buildNumber: String? = null,
)
