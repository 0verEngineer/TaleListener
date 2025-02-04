
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.overengineer.talelistener.domain.BookChapter
import org.overengineer.talelistener.ui.extensions.formatLeadingMinutes
import talelistener.composeapp.generated.resources.Res
import talelistener.composeapp.generated.resources.player_screen_now_playing_title

@Composable
fun PlaylistItemComposable(
    track: BookChapter,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ),
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Outlined.Audiotrack,
                contentDescription = stringResource(Res.string.player_screen_now_playing_title),
                modifier = Modifier.size(16.dp),
            )
        } else {
            Spacer(modifier = Modifier.size(16.dp))
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = track.title,
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onBackground,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = track.duration.toInt().formatLeadingMinutes(),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 8.dp),
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = colorScheme.onBackground.copy(alpha = 0.6f),
        )
    }
}
