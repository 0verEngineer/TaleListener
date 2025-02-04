
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.domain
 * Modifications:
 * - Updated package statement
 */

package org.overengineer.talelistener.domain

sealed interface TimerOption

class DurationTimerOption(val duration: Int) : TimerOption
data object CurrentEpisodeTimerOption : TimerOption
