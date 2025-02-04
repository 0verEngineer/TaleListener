
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.ui.screens.player.composable
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Removed ImageLoader
 */

package org.overengineer.talelistener.ui.screen.player.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.overengineer.talelistener.channel.common.LibraryType
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences
import org.overengineer.talelistener.ui.components.AsyncShimmeringImage
import org.overengineer.talelistener.ui.viewmodel.PlayerViewModel
import talelistener.composeapp.generated.resources.Res
import talelistener.composeapp.generated.resources.audiobook_fallback
import talelistener.composeapp.generated.resources.player_screen_now_playing_title_chapter_of
import talelistener.composeapp.generated.resources.podcast_fallback


@Composable
fun TrackDetailsComposable(
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
    settings: TaleListenerSharedPreferences
) {
    val currentTrackIndex by viewModel.currentChapterIndex.collectAsState(0)
    val book by viewModel.book.collectAsState()

    val libraryType = settings.getPreferredLibrary()?.type ?: throw IllegalStateException("Library is missing")

    val token = settings.getToken() ?: throw IllegalStateException("Token is missing")
    val host = settings.getHost() ?: throw IllegalStateException("Host is missing")
    val customHeaders = settings.getCustomHeaders()

    // todo: do it everywhere screen size was used https://stackoverflow.com/questions/77341731/how-to-get-screen-width-and-height-in-compose-multiplatform
    //val configuration = LocalConfiguration.current
    //val screenHeight = configuration.screenHeightDp.dp
    //val maxImageHeight = screenHeight * 0.33f
    val maxImageHeight = 300.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        AsyncShimmeringImage(
            token = token,
            host = host,
            itemId = book!!.id,
            customHeaders = customHeaders,
            contentDescription = "${book?.title} cover",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .heightIn(max = maxImageHeight)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp)),
            error = when (libraryType) { // todo: test this
                LibraryType.LIBRARY -> painterResource(Res.drawable.audiobook_fallback)
                LibraryType.PODCAST -> painterResource(Res.drawable.podcast_fallback)
                LibraryType.UNKNOWN -> painterResource(Res.drawable.audiobook_fallback)
            },
            size = coil3.size.Size.ORIGINAL
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = book?.title.orEmpty(),
            style = typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onBackground,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(
                Res.string.player_screen_now_playing_title_chapter_of,
                currentTrackIndex + 1,
                book?.chapters?.size ?: "?",
            ),
            style = typography.bodySmall,
            color = colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
