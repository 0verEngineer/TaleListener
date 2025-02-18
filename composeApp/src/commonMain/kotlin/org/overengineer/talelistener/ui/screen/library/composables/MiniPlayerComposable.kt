
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.ui.screens.library.composable
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Migration to kotlin multiplatform
 * - Removed imageLoader param
 * - Added host, token and headers param for TaleListener's AsyncShimmeringImage impl
 */

package org.overengineer.talelistener.ui.screen.library.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import org.jetbrains.compose.resources.painterResource
import org.overengineer.talelistener.domain.DetailedItem
import org.overengineer.talelistener.domain.connection.ServerRequestHeader
import org.overengineer.talelistener.ui.components.AsyncShimmeringImage
import org.overengineer.talelistener.ui.screen.player.PlayerScreen
import org.overengineer.talelistener.ui.viewmodel.PlayerViewModel
import talelistener.composeapp.generated.resources.Res
import talelistener.composeapp.generated.resources.audiobook_fallback

@Composable
fun MiniPlayerComposable(
    host: String,
    token: String,
    customHeaders: List<ServerRequestHeader>,
    navigator: Navigator,
    modifier: Modifier = Modifier,
    book: DetailedItem,
    playerViewModel: PlayerViewModel,
) {
    val isPlaying: Boolean by playerViewModel.isPlaying.collectAsState(false)

    Surface(
        shadowElevation = 4.dp,
        modifier = modifier.clickable { navigator.push(PlayerScreen(book.id, book.title)) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncShimmeringImage(
                host = host,
                token = token,
                itemId = book.id,
                customHeaders = customHeaders,
                contentDescription = "${book.title} cover",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .size(48.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(4.dp)),
                error = painterResource(Res.drawable.audiobook_fallback), // todo: Podcast?
                size = coil3.size.Size.ORIGINAL
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                book
                    .author
                    ?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Row {
                    IconButton(
                        onClick = { playerViewModel.togglePlayPause() },
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Outlined.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }
            }
        }
    }
}
