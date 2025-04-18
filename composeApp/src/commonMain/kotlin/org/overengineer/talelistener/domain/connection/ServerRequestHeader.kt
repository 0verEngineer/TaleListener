
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.domain.connection
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Replaced java UUID with a String
 */

package org.overengineer.talelistener.domain.connection

import kotlinx.serialization.Serializable
import org.overengineer.talelistener.platform.randomUUID


@Serializable
data class ServerRequestHeader(
    val name: String,
    val value: String,
    val id: String = randomUUID(),
) {

    companion object {
        fun empty() = ServerRequestHeader("", "")

        fun ServerRequestHeader.clean(): ServerRequestHeader {
            val name = this.name.clean()
            val value = this.value.clean()

            return this.copy(name = name, value = value)
        }

        private fun String.clean(): String {
            var sanitized = this.replace(Regex("[\\r\\n]"), "")
            sanitized = sanitized.replace(Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]"), "")
            sanitized = sanitized.trim()

            return sanitized
        }
    }
}
