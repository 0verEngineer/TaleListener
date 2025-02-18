
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.ui.screens.library.composables
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Migration to coil3
 * - Removed imageRequest and moved it into AsyncShimmeringImage
 * - Removed ImageLoader and passed the settings instead to have token and host
 * - Changed navController to navigator
 */

package org.overengineer.talelistener.ui.screen.library.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import org.jetbrains.compose.resources.painterResource
import org.overengineer.talelistener.channel.common.LibraryType
import org.overengineer.talelistener.domain.RecentBook
import org.overengineer.talelistener.domain.connection.ServerRequestHeader
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences
import org.overengineer.talelistener.ui.components.AsyncShimmeringImage
import org.overengineer.talelistener.ui.screen.player.PlayerScreen
import org.overengineer.talelistener.ui.theme.TLOrange
import org.overengineer.talelistener.ui.viewmodel.LibraryViewModel
import talelistener.composeapp.generated.resources.Res
import talelistener.composeapp.generated.resources.audiobook_fallback


@Composable
fun RecentBooksComposable(
    navigator: Navigator,
    recentBooks: List<RecentBook>,
    modifier: Modifier = Modifier,
    libraryViewModel: LibraryViewModel,
    settings: TaleListenerSharedPreferences
) {
    val itemWidth = 120.dp

    val token = settings.getToken() ?: throw IllegalStateException("Token is missing")
    val host = settings.getHost() ?: throw IllegalStateException("Host is missing")
    val customHeaders = settings.getCustomHeaders()

    // todo horizontal scroll with touchpad gesture not working on linux: https://youtrack.jetbrains.com/issue/CMP-2690
    //  - test on windows and macos
    //  - comment the issue

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        recentBooks
            .forEach { book ->
                RecentBookItemComposable(
                    book = book,
                    token = token,
                    host = host,
                    customHeaders = customHeaders,
                    width = itemWidth,
                    navigator = navigator,
                    libraryViewModel = libraryViewModel,
                )
            }
    }
}

@Composable
fun RecentBookItemComposable(
    token: String,
    host: String,
    customHeaders: List<ServerRequestHeader>,
    navigator: Navigator,
    book: RecentBook,
    width: Dp,
    libraryViewModel: LibraryViewModel,
) {
    Column(
        modifier = Modifier
            .width(width)
            .clickable {
                navigator.push(PlayerScreen(book.id, book.title))
            },
    ) {
        var coverLoading by remember { mutableStateOf(true) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .aspectRatio(1f),
        ) {
            AsyncShimmeringImage(
                token = token,
                host = host,
                itemId = book.id,
                customHeaders = customHeaders,
                contentDescription = "${book.title} cover",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                error = painterResource(Res.drawable.audiobook_fallback),
                onLoadingStateChanged = { coverLoading = it },
                size = coil3.size.Size.ORIGINAL
            )

            if (!coverLoading && shouldShowProgress(book, libraryViewModel.fetchPreferredLibraryType())) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .align(Alignment.BottomCenter),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Gray.copy(alpha = 0.4f)),
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(calculateProgress(book))
                            .fillMaxHeight()
                            .background(TLOrange),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.padding(horizontal = 4.dp)) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(4.dp))

            book.author?.let {
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onBackground.copy(
                            alpha = 0.6f,
                        ),
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun calculateProgress(book: RecentBook): Float {
    return book.listenedPercentage?.div(100.0f) ?: 0.0f
}

private fun shouldShowProgress(book: RecentBook, libraryType: LibraryType): Boolean =
    book.listenedPercentage != null &&
        libraryType == LibraryType.LIBRARY &&
        book.listenedPercentage > 0
