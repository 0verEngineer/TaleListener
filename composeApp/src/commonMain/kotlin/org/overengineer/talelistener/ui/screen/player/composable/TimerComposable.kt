
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.ui.screens.player.composable
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Migrated to kotlin multiplatform
 */

package org.overengineer.talelistener.ui.screen.player.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.overengineer.talelistener.domain.CurrentEpisodeTimerOption
import org.overengineer.talelistener.domain.DurationTimerOption
import org.overengineer.talelistener.domain.TimerOption
import talelistener.composeapp.generated.resources.Res
import talelistener.composeapp.generated.resources.timer_option_after_current_episode
import talelistener.composeapp.generated.resources.timer_option_after_minutes
import talelistener.composeapp.generated.resources.timer_option_disable_timer
import talelistener.composeapp.generated.resources.timer_title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerComposable(
    currentOption: TimerOption?,
    onOptionSelected: (TimerOption?) -> Unit,
    onDismissRequest: () -> Unit,
) {

    ModalBottomSheet(
        containerColor = colorScheme.background,
        onDismissRequest = onDismissRequest,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(Res.string.timer_title),
                    style = typography.bodyLarge,
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    itemsIndexed(TimerOptions) { index, item ->
                        ListItem(
                            headlineContent = {
                                Row {
                                    Text(
                                        text = when (item) {
                                            CurrentEpisodeTimerOption -> stringResource(Res.string.timer_option_after_current_episode)
                                            is DurationTimerOption -> stringResource(Res.string.timer_option_after_minutes, item.duration)
                                        }
                                    )
                                }
                            },
                            trailingContent = {
                                if (item == currentOption) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onOptionSelected(item)
                                    onDismissRequest()
                                },
                        )
                        if (index < TimerOptions.size - 1) {
                            HorizontalDivider()
                        }
                    }

                    if (currentOption != null) {
                        item {
                            HorizontalDivider()

                            ListItem(
                                headlineContent = {
                                    Row {
                                        Text(
                                            text = stringResource(Res.string.timer_option_disable_timer),
                                            color = colorScheme.error,
                                            style = typography.bodyMedium,
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onOptionSelected(null)
                                        onDismissRequest()
                                    },
                            )
                        }
                    }
                }
            }
        },
    )
}

private val TimerOptions = listOf(
    DurationTimerOption(10),
    DurationTimerOption(15),
    DurationTimerOption(30),
    DurationTimerOption(60),
    CurrentEpisodeTimerOption,
)
