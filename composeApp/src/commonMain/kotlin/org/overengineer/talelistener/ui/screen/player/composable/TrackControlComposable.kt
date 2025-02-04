

/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.ui.screens.player.composable
 * Modifications:
 * - Updated package statement and adjusted imports.
 */

package org.overengineer.talelistener.ui.screen.player.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Forward30
import androidx.compose.material.icons.rounded.PauseCircleFilled
import androidx.compose.material.icons.rounded.PlayCircleFilled
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.overengineer.talelistener.ui.extensions.formatFully
import org.overengineer.talelistener.ui.viewmodel.PlayerViewModel

@Composable
fun TrackControlComposable(
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
) {
    val isPlaying by viewModel.isPlaying.collectAsState(false)
    val playbackReady by viewModel.isPlaybackReady.collectAsState(false)
    val currentTrackIndex by viewModel.currentChapterIndex.collectAsState(0)
    val currentTrackPosition by viewModel.currentChapterPosition.collectAsState(0.0)
    val currentTrackDuration by viewModel.currentChapterDuration.collectAsState(0.0)

    val book by viewModel.book.collectAsState()
    val chapters = book?.chapters ?: emptyList()

    var sliderPosition by remember { mutableStateOf(0.0) }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(currentTrackPosition, currentTrackIndex, currentTrackDuration) {
        if (playbackReady && !isDragging) {
            sliderPosition = currentTrackPosition
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Slider(
                value = sliderPosition.toFloat(),
                onValueChange = { newPosition ->
                    isDragging = true
                    sliderPosition = newPosition.toDouble()
                },
                onValueChangeFinished = {
                    isDragging = false
                    viewModel.seekTo(sliderPosition)
                },
                valueRange = 0f..currentTrackDuration.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = colorScheme.primary,
                    activeTrackColor = colorScheme.primary,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = currentTrackPosition.toInt().formatFully(),
                        style = typography.bodySmall,
                        color = colorScheme.onBackground.copy(alpha = 0.6f),
                    )
                    Text(
                        text = maxOf(0.0, currentTrackDuration - currentTrackPosition)
                            .toInt()
                            .formatFully(),
                        style = typography.bodySmall,
                        color = colorScheme.onBackground.copy(alpha = 0.6f),
                    )
                }

                Spacer(modifier = Modifier.height(64.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        viewModel.previousTrack()
                    },
                    enabled = true,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous Track",
                        tint = colorScheme.onBackground,
                        modifier = Modifier.size(36.dp),
                    )
                }

                IconButton(
                    onClick = { viewModel.rewind() },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Replay10,
                        contentDescription = "Rewind",
                        tint = colorScheme.onBackground,
                        modifier = Modifier.size(48.dp),
                    )
                }

                IconButton(
                    onClick = { viewModel.togglePlayPause() },
                    modifier = Modifier.size(72.dp),
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.PauseCircleFilled else Icons.Rounded.PlayCircleFilled,
                        contentDescription = "Play / Pause",
                        tint = colorScheme.primary,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                IconButton(
                    onClick = { viewModel.forward() },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Forward30,
                        contentDescription = "Forward",
                        tint = colorScheme.onBackground,
                        modifier = Modifier.size(48.dp),
                    )
                }

                IconButton(
                    onClick = {
                        if (currentTrackIndex < chapters.size - 1) {
                            viewModel.nextTrack()
                        }
                    },
                    enabled = currentTrackIndex < chapters.size - 1,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next Track",
                        tint = if (currentTrackIndex < chapters.size - 1) {
                            colorScheme.onBackground
                        } else colorScheme.onBackground.copy(
                            alpha = 0.3f,
                        ),
                        modifier = Modifier.size(36.dp),
                    )
                }
            }
        }
    }
}
