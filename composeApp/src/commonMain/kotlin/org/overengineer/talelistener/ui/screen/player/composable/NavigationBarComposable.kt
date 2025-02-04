
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.ui.screens.player.composable
 * Modifications:
 * - Updated package statement and adjusted imports.
 */

package org.overengineer.talelistener.ui.screen.player.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Headset
import androidx.compose.material.icons.outlined.SlowMotionVideo
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.Navigator
import org.jetbrains.compose.resources.stringResource
import org.overengineer.talelistener.ui.icons.TimerPlay
import org.overengineer.talelistener.ui.screen.library.LibraryScreen
import org.overengineer.talelistener.ui.viewmodel.PlayerViewModel
import talelistener.composeapp.generated.resources.Res
import talelistener.composeapp.generated.resources.player_screen_chapter_list_navigation
import talelistener.composeapp.generated.resources.player_screen_library_navigation
import talelistener.composeapp.generated.resources.player_screen_playback_speed_navigation
import talelistener.composeapp.generated.resources.player_screen_timer_navigation

@Composable
fun NavigationBarComposable(
    viewModel: PlayerViewModel,
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    val timerOption by viewModel.timerOption.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState(1f)
    val playingQueueExpanded by viewModel.playingQueueExpanded.collectAsState(false)

    var playbackSpeedExpanded by remember { mutableStateOf(false) }
    var timerExpanded by remember { mutableStateOf(false) }

    Surface(
        shadowElevation = 4.dp,
        modifier = modifier.height(64.dp),
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            contentColor = colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
        ) {
            val iconSize = 24.dp
            val labelStyle = typography.labelSmall.copy(fontSize = 10.sp)

            NavigationBarItem(
                icon = {
                    Icon(
                        Icons.Outlined.Headset,
                        contentDescription = stringResource(Res.string.player_screen_library_navigation),
                        modifier = Modifier.size(iconSize),
                    )
                },
                label = {
                    Text(
                        text = stringResource(Res.string.player_screen_library_navigation),
                        style = labelStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                selected = false,
                onClick = { navigator.replaceAll(LibraryScreen()) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colorScheme.primary,
                    indicatorColor = colorScheme.surfaceContainer,
                ),
            )

            NavigationBarItem(
                icon = {
                    Icon(
                        Icons.Outlined.Book,
                        contentDescription = stringResource(Res.string.player_screen_chapter_list_navigation),
                        modifier = Modifier.size(iconSize),
                    )
                },
                label = {
                    Text(
                        text = stringResource(Res.string.player_screen_chapter_list_navigation),
                        style = labelStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                selected = playingQueueExpanded,
                onClick = { viewModel.togglePlayingQueue() },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colorScheme.primary,
                    indicatorColor = colorScheme.surfaceContainer,
                ),
            )

            NavigationBarItem(
                icon = {
                    Icon(
                        Icons.Outlined.SlowMotionVideo,
                        contentDescription = stringResource(Res.string.player_screen_playback_speed_navigation),
                        modifier = Modifier.size(iconSize),
                    )
                },
                label = {
                    Text(
                        text = stringResource(Res.string.player_screen_playback_speed_navigation),
                        style = labelStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                selected = false,
                onClick = { playbackSpeedExpanded = true },
                enabled = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colorScheme.primary,
                    indicatorColor = colorScheme.surfaceContainer,
                ),
            )

            NavigationBarItem(
                icon = {
                    Icon(
                        when (timerOption) {
                            null -> Icons.Outlined.Timer
                            else -> TimerPlay
                        },
                        contentDescription = stringResource(Res.string.player_screen_timer_navigation),
                        modifier = Modifier.size(iconSize),
                    )
                },
                label = {
                    Text(
                        text = stringResource(Res.string.player_screen_timer_navigation),
                        style = labelStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                selected = false,
                onClick = { timerExpanded = true },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colorScheme.primary,
                    indicatorColor = colorScheme.surfaceContainer,
                ),
            )

            if (playbackSpeedExpanded) {
                PlaybackSpeedComposable(
                    currentSpeed = playbackSpeed,
                    onSpeedChange = { viewModel.setPlaybackSpeed(it) },
                    onDismissRequest = { playbackSpeedExpanded = false },
                )
            }

            if (timerExpanded) {
                TimerComposable(
                    currentOption = timerOption,
                    onOptionSelected = { viewModel.setTimer(it) },
                    onDismissRequest = { timerExpanded = false },
                )
            }
        }
    }
}
