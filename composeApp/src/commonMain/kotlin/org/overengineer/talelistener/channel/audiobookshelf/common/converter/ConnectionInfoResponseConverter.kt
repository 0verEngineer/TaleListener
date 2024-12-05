
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.audiobookshelf.common.converter
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Removed javax singleton stuff
 */

package org.overengineer.talelistener.channel.audiobookshelf.common.converter

import org.overengineer.talelistener.channel.audiobookshelf.common.model.connection.ConnectionInfoResponse
import org.overengineer.talelistener.channel.common.ConnectionInfo

class ConnectionInfoResponseConverter constructor() {

    fun apply(response: ConnectionInfoResponse): ConnectionInfo = ConnectionInfo(
        username = response.user.username,
        serverVersion = response.serverSettings?.version,
        buildNumber = response.serverSettings?.buildNumber,
    )
}
