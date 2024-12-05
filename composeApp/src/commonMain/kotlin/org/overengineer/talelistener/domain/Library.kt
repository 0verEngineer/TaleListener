
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.common
 * Modifications: Updated package statement and adjusted imports.
 */

package org.overengineer.talelistener.domain

import org.overengineer.talelistener.channel.common.LibraryType

data class Library(
    val id: String,
    val title: String,
    val type: LibraryType,
)
