/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.audiobookshelf.common.api
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Removed javax singleton stuff
 */

package org.overengineer.talelistener.channel.audiobookshelf.common.api

import org.overengineer.talelistener.domain.connection.ServerRequestHeader
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences

class RequestHeadersProvider (
    private val preferences: TaleListenerSharedPreferences,
) {

    fun fetchRequestHeaders(): List<ServerRequestHeader> {
        return preferences.getCustomHeaders()
    }
}
