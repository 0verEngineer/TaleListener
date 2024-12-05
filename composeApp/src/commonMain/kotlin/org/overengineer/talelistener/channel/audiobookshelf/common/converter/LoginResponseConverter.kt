
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.audiobookshelf.common.converter
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Removed javax singleton stuff
 */

package org.overengineer.talelistener.channel.audiobookshelf.common.converter

import org.overengineer.talelistener.channel.audiobookshelf.common.model.user.LoggedUserResponse
import org.overengineer.talelistener.domain.UserAccount

class LoginResponseConverter constructor() {

    fun apply(response: LoggedUserResponse): UserAccount = UserAccount(
        token = response.user.token,
        preferredLibraryId = response.userDefaultLibraryId,
    )
}
