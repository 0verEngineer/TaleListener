
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.ui.screens.library.composables.fallback
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Removed LocalConfiguration and screenHeight stuff
 * - Migrated localization messages
 */

package org.overengineer.talelistener.ui.screen.library.composables.fallback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.overengineer.talelistener.channel.common.LibraryType
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences
import org.overengineer.talelistener.platform.NetworkQualityService
import org.overengineer.talelistener.ui.viewmodel.LibraryViewModel
import talelistener.composeapp.generated.resources.Res
import talelistener.composeapp.generated.resources.library_is_empty
import talelistener.composeapp.generated.resources.no_internet_connection
import talelistener.composeapp.generated.resources.offline_library_is_empty
import talelistener.composeapp.generated.resources.offline_podcast_library_is_empty

@Composable
fun LibraryFallbackComposable(
    searchRequested: Boolean,
    libraryViewModel: LibraryViewModel,
    networkQualityService: NetworkQualityService,
    settings: TaleListenerSharedPreferences
) {
    // todo: does this cause problems if we hardcode it?
    val screenHeight = 300.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(screenHeight / 2),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val hasNetwork = networkQualityService.isNetworkAvailable()
            val isConnected = settings.isConnectedAndOnline()

            val text = when {
                searchRequested -> null
                !isConnected -> when (libraryViewModel.fetchPreferredLibraryType()) {
                    LibraryType.PODCAST -> stringResource(Res.string.offline_podcast_library_is_empty)
                    else -> stringResource(Res.string.offline_library_is_empty)
                }
                hasNetwork.not() -> stringResource(Res.string.no_internet_connection)
                else -> stringResource(Res.string.library_is_empty)
            }

            val icon = when {
                searchRequested -> null
                !isConnected -> Icons.AutoMirrored.Filled.LibraryBooks
                hasNetwork.not() -> Icons.Filled.WifiOff
                else -> Icons.AutoMirrored.Filled.LibraryBooks
            }

            icon?.let {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = it,
                        contentDescription = "Library placeholder",
                        tint = Color.White,
                        modifier = Modifier.size(64.dp),
                    )
                }
            }

            text?.let {
                Text(
                    textAlign = TextAlign.Center,
                    text = it,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 36.dp),
                )
            }
        }
    }
}
