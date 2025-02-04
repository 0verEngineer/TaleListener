
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.ui.screens.player
 * Modifications:
 * - Updated package statement
 * - Migrated to kotlin multiplatform
 */

package org.overengineer.talelistener.ui.screen.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences
import org.overengineer.talelistener.ui.screen.player.composable.NavigationBarComposable
import org.overengineer.talelistener.ui.screen.player.composable.PlayingQueueComposable
import org.overengineer.talelistener.ui.screen.player.composable.TrackControlComposable
import org.overengineer.talelistener.ui.screen.player.composable.TrackDetailsComposable
import org.overengineer.talelistener.ui.screen.player.composable.placeholder.PlayingQueuePlaceholderComposable
import org.overengineer.talelistener.ui.screen.player.composable.placeholder.TrackDetailsPlaceholderComposable
import org.overengineer.talelistener.ui.viewmodel.PlayerViewModel
import talelistener.composeapp.generated.resources.Res
import talelistener.composeapp.generated.resources.player_screen_now_playing_title
import talelistener.composeapp.generated.resources.player_screen_title

class PlayerScreen(
    private val bookId: String,
    private val bookTitle: String
): Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = koinInject<PlayerViewModel>()
        val settings = koinInject<TaleListenerSharedPreferences>()
        val navigator = LocalNavigator.currentOrThrow

        val playingBook by viewModel.book.collectAsState()
        val isPlaybackReady by viewModel.isPlaybackReady.collectAsState()
        val playingQueueExpanded by viewModel.playingQueueExpanded.collectAsState()
        val searchRequested by viewModel.searchRequested.collectAsState()

        val screenTitle = when (playingQueueExpanded) {
            true -> stringResource(Res.string.player_screen_now_playing_title)
            false -> stringResource(Res.string.player_screen_title)
        }

        val titleTextStyle = typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)

        // todo: Lissen has a BackHandler here so that when a back gesture is fired when the playingQueueExpanded it is closed instead of a whole nav back
        //  -> this also closes the search if we are in the search thing
        //  -> BackHandler support is already merged: https://github.com/JetBrains/compose-multiplatform-core/pull/1771

        LaunchedEffect(bookId) {
            bookId
                .takeIf { it != playingBook?.id }
                ?.let { viewModel.preparePlayback(it) }
        }

        LaunchedEffect(playingQueueExpanded) {
            if (playingQueueExpanded.not()) {
                viewModel.dismissSearch()
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    actions = {
                        if (playingQueueExpanded) {
                            AnimatedContent(
                                targetState = searchRequested,
                                label = "library_action_animation",
                                transitionSpec = {
                                    fadeIn(animationSpec = keyframes { durationMillis = 150 }) togetherWith
                                            fadeOut(animationSpec = keyframes { durationMillis = 150 })
                                },
                            ) { isSearchRequested ->
                                when (isSearchRequested) {
                                    true -> ChapterSearchActionComposable(
                                        onSearchRequested = { viewModel.updateSearch(it) },
                                    )

                                    false -> Row {
                                        IconButton(
                                            onClick = { viewModel.requestSearch() },
                                            modifier = Modifier.padding(end = 4.dp),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Search,
                                                contentDescription = null,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    title = {
                        Text(
                            text = screenTitle,
                            style = titleTextStyle,
                            color = colorScheme.onSurface,
                            maxLines = 1,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = colorScheme.onSurface,
                            )
                        }
                    },
                )
            },
            bottomBar = {
                NavigationBarComposable(
                    viewModel = viewModel,
                    navigator = navigator
                )
            },
            modifier = Modifier.systemBarsPadding(),
            content = { innerPadding ->
                Column(
                    modifier = Modifier
                        .testTag("playerScreen")
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AnimatedVisibility(
                        visible = playingQueueExpanded.not(),
                        enter = expandVertically(animationSpec = tween(400)),
                        exit = shrinkVertically(animationSpec = tween(400)),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            if (!isPlaybackReady) {
                                TrackDetailsPlaceholderComposable(bookTitle)
                            } else {
                                TrackDetailsComposable(
                                    viewModel = viewModel,
                                    settings = settings
                                )
                            }

                            TrackControlComposable(
                                viewModel = viewModel,
                                modifier = Modifier,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isPlaybackReady) {
                        PlayingQueueComposable(
                            viewModel = viewModel,
                            modifier = Modifier,
                        )
                    } else {
                        PlayingQueuePlaceholderComposable(
                            modifier = Modifier,
                        )
                    }
                }
            },
        )
    }
}