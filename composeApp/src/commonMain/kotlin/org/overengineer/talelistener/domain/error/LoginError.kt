
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.domain
 * Modifications:
 * - Updated package statement and adjusted imports.
 */

package org.overengineer.talelistener.domain.error

sealed class LoginError {
    data object Unauthorized : LoginError()
    data object NetworkError : LoginError()
    data object InvalidCredentialsHost : LoginError()
    data object MissingCredentialsHost : LoginError()
    data object MissingCredentialsUsername : LoginError()
    data object MissingCredentialsPassword : LoginError()
    data object InternalError : LoginError()
}
