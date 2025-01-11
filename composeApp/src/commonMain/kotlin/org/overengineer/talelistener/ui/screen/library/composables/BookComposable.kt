
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.ui.screens.library.composables
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Removed ImageLoader
 * - Changed NavController to navigator
 * - Added host and token as parameter
 */

package org.overengineer.talelistener.ui.screen.library.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Downloading
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.overengineer.talelistener.content.cache.CacheProgress
import org.overengineer.talelistener.domain.Book
import org.overengineer.talelistener.domain.BookCachedState
import org.overengineer.talelistener.domain.connection.ServerRequestHeader
import org.overengineer.talelistener.ui.components.AsyncShimmeringImage
import org.overengineer.talelistener.common.BookCacheAction
import org.overengineer.talelistener.ui.extensions.formatShortly
import org.overengineer.talelistener.ui.viewmodel.CachingViewModel
import talelistener.composeapp.generated.resources.Res
import talelistener.composeapp.generated.resources.audiobook_fallback
import talelistener.composeapp.generated.resources.dialog_cancel
import talelistener.composeapp.generated.resources.dialog_confirm_remove
import talelistener.composeapp.generated.resources.dialog_remove_from_cache_text
import talelistener.composeapp.generated.resources.dialog_remove_from_cache_title

@Composable
fun BookComposable(
    book: Book,
    host: String,
    token: String,
    customHeaders: List<ServerRequestHeader>,
    navigator: Navigator,
    cachingViewModel: CachingViewModel,
    onRemoveBook: () -> Unit,
) {
    val cacheProgress by cachingViewModel.getCacheProgress(book.id).collectAsState()
    var showDeleteFromCacheDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /*navController.showPlayer(book.id, book.title) todo player*/ }
            .testTag("bookItem_${book.id}")
            .padding(horizontal = 4.dp, vertical = 8.dp),
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
                .size(64.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(4.dp)),
            error = painterResource(Res.drawable.audiobook_fallback),
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
                    Spacer(modifier = Modifier.height(4.dp))
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

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            provideCachingStateIcon(book, cacheProgress)
                ?.let { icon ->
                    IconButton(
                        onClick = {
                            cachingViewModel
                                .provideCacheAction(book)
                                ?.let {
                                    when (it) {
                                        BookCacheAction.CACHE -> cachingViewModel.cacheBook(book)
                                        BookCacheAction.DROP -> {
                                            showDeleteFromCacheDialog = true
                                        }
                                    }
                                }
                        },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = "Caching Book State",
                        )
                    }
                }

            if (book.duration > 0) {
                Text(
                    text = book.duration.formatShortly(),
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(Modifier.height(6.dp))
        }
    }

    if (showDeleteFromCacheDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteFromCacheDialog = false },
            title = { Text(text = stringResource(Res.string.dialog_remove_from_cache_title)) },
            text = { Text(stringResource(Res.string.dialog_remove_from_cache_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        cachingViewModel.dropCache(book)
                        onRemoveBook.invoke()
                        showDeleteFromCacheDialog = false
                    },
                ) {
                    Text(
                        text = stringResource(Res.string.dialog_confirm_remove),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteFromCacheDialog = false }) {
                    Text(
                        text = stringResource(Res.string.dialog_cancel),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.medium,
        )
    }
}

private fun provideCachingStateIcon(
    book: Book,
    cacheProgress: CacheProgress,
): ImageVector? = when (cacheProgress) {
    CacheProgress.Completed -> cachedIcon
    CacheProgress.Removed -> ableToCacheIcon
    CacheProgress.Error -> ableToCacheIcon
    CacheProgress.Idle -> provideIdleStateIcon(book)
    is CacheProgress.Caching -> cachingIcon
}

private fun provideIdleStateIcon(book: Book): ImageVector? = when (book.cachedState) {
    BookCachedState.ABLE_TO_CACHE -> ableToCacheIcon
    BookCachedState.CACHED -> cachedIcon
    BookCachedState.UNABLE_TO_CACHE -> null
}

private val ableToCacheIcon = Icons.Outlined.Cloud
private val cachedIcon = Icons.Outlined.CloudDownload
private val cachingIcon = Icons.Outlined.Downloading
