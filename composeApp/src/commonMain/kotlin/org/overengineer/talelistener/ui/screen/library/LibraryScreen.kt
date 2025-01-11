
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.ui.screens.library
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Migration to kotlin multiplatform
 * - Refactoring for TaleListeners offline detection
 */

package org.overengineer.talelistener.ui.screen.library

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import app.cash.paging.compose.collectAsLazyPagingItems
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.aakira.napier.Napier
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.overengineer.talelistener.channel.common.LibraryType
import org.overengineer.talelistener.domain.RecentBook
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences
import org.overengineer.talelistener.platform.NetworkQualityService
import org.overengineer.talelistener.ui.screen.library.composables.BookComposable
import org.overengineer.talelistener.ui.screen.library.composables.DefaultActionComposable
import org.overengineer.talelistener.ui.screen.library.composables.LibrarySearchActionComposable
import org.overengineer.talelistener.ui.screen.library.composables.RecentBooksComposable
import org.overengineer.talelistener.ui.screen.library.composables.fallback.LibraryFallbackComposable
import org.overengineer.talelistener.ui.screen.library.composables.placeholder.LibraryPlaceholderComposable
import org.overengineer.talelistener.ui.screen.library.composables.placeholder.RecentBooksPlaceholderComposable
import org.overengineer.talelistener.ui.viewmodel.CachingViewModel
import org.overengineer.talelistener.ui.viewmodel.LibraryViewModel
import talelistener.composeapp.generated.resources.Res
import talelistener.composeapp.generated.resources.continue_listening
import talelistener.composeapp.generated.resources.library_title
import talelistener.composeapp.generated.resources.podcast_library_title
import withMinimumTime


// todo Lissen uses a BackHandler to dismiss the search, how to do it? - do we need it?
class LibraryScreen: Screen {
    @OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val settings = koinInject<TaleListenerSharedPreferences>()
        val libraryViewModel = koinInject<LibraryViewModel>()
        val cachingViewModel = koinInject<CachingViewModel>()
        val networkQualityService = koinInject<NetworkQualityService>()
        val navigator = LocalNavigator.currentOrThrow

        val coroutineScope = rememberCoroutineScope()

        val titleTextStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
        val titleHeightDp = with(LocalDensity.current) { titleTextStyle.lineHeight.toPx().toDp() }

        val recentBooks: List<RecentBook> by libraryViewModel.recentBooks.collectAsState()
        val hiddenBooks by libraryViewModel.hiddenBooks.collectAsState()

        val recentBookRefreshing by libraryViewModel.recentBookUpdating.collectAsState()
        val searchRequested by libraryViewModel.searchRequested.collectAsState()

        var pullRefreshing by remember { mutableStateOf(false) }

        val library = when (searchRequested) {
            true -> libraryViewModel.searchPager.collectAsLazyPagingItems()
            false -> libraryViewModel.libraryPager.collectAsLazyPagingItems()
        }

        val showingRecentBooks by remember(recentBooks, hiddenBooks) {
            derivedStateOf { filterRecentBooks(recentBooks, libraryViewModel) }
        }

        val host = settings.getHost() ?: throw IllegalStateException("Host is missing")
        val token = settings.getToken() ?: throw IllegalStateException("Token is missing")
        val customHeaders = settings.getCustomHeaders()

        val networkStatus by networkQualityService.networkStatus.collectAsState()

        val libraryTitle = stringResource(Res.string.library_title)
        val podcastTitle = stringResource(Res.string.podcast_library_title)
        val continueListeningTitle = stringResource(Res.string.continue_listening)

        fun refreshContent(showRefreshing: Boolean) {
            Napier.d("LibraryScreen refreshContent")
            coroutineScope.launch {
                if (showRefreshing) {
                    pullRefreshing = true
                }

                withMinimumTime(500) {
                    listOf(
                        async { libraryViewModel.dropHiddenBooks() },
                        async { libraryViewModel.refreshLibrary() },
                        async { libraryViewModel.fetchRecentListening() },
                    ).awaitAll()
                }

                pullRefreshing = false
            }
        }

        val pullRefreshState = rememberPullRefreshState(
            refreshing = pullRefreshing,
            onRefresh = {
                refreshContent(showRefreshing = true)
            },
        )

        val isPlaceholderRequired by remember {
            derivedStateOf {
                if (searchRequested) {
                    return@derivedStateOf false
                }

                pullRefreshing || recentBookRefreshing || library.loadState.refresh is LoadState.Loading
            }
        }

        val libraryListState = rememberLazyListState()

        // todo player
        //val playingBook by playerViewModel.book.observeAsState()

        fun showRecent(): Boolean {
            val fetchAvailable = settings.isConnectedAndOnline()
            val hasContent = showingRecentBooks.isEmpty().not()
            return !searchRequested && hasContent && fetchAvailable
        }

        LaunchedEffect(networkStatus) {
            refreshContent(false) // todo test on real device
        }

        LaunchedEffect(Unit) {
            libraryViewModel.refreshRecentListening()
            libraryViewModel.refreshLibrary()
        }

        LaunchedEffect(searchRequested) {
            if (!searchRequested) {
                libraryListState.scrollToItem(0)
            }
        }

        fun provideLibraryTitle(): String {
            val type = libraryViewModel.fetchPreferredLibraryType()

            return when (type) {
                LibraryType.LIBRARY -> libraryTitle
                LibraryType.PODCAST -> podcastTitle
                LibraryType.UNKNOWN -> ""
            }
        }

        val navBarTitle by remember {
            derivedStateOf {
                val showRecent = showRecent()
                val recentBlockVisible = libraryListState.layoutInfo.visibleItemsInfo.firstOrNull()?.key == "recent_books"

                when {
                    showRecent && recentBlockVisible -> continueListeningTitle
                    else -> provideLibraryTitle()
                }
            }
        }


        Scaffold(
            topBar = {
                TopAppBar( // todo experimental, change?
                    actions = {
                        AnimatedContent(
                            targetState = searchRequested,
                            label = "library_action_animation",
                            transitionSpec = {
                                fadeIn(animationSpec = keyframes { durationMillis = 150 }) togetherWith
                                        fadeOut(animationSpec = keyframes { durationMillis = 150 })
                            },
                        ) { isSearchRequested ->
                            when (isSearchRequested) {
                                true -> LibrarySearchActionComposable(
                                    onSearchDismissed = { libraryViewModel.dismissSearch() },
                                    onSearchRequested = { libraryViewModel.updateSearch(it) },
                                )

                                false -> DefaultActionComposable(
                                    navigator = navigator,
                                    cachingViewModel = cachingViewModel,
                                    libraryViewModel = libraryViewModel,
                                    onContentRefreshing = { refreshContent(showRefreshing = false) },
                                    onSearchRequested = { libraryViewModel.requestSearch() },
                                )
                            }
                        }
                    },
                    title = {
                        if (!searchRequested) {
                            Text(
                                text = navBarTitle,
                                style = titleTextStyle,
                                maxLines = 1,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    },
                    modifier = Modifier.systemBarsPadding(),
                )
            },
            bottomBar = {
                // todo player
                /*playingBook?.let {
                    MiniPlayerComposable(
                        navController = navController,
                        book = it,
                        imageLoader = imageLoader,
                        playerViewModel = playerViewModel,
                    )
                }*/
            },
            modifier = Modifier
                .systemBarsPadding()
                .fillMaxSize(),
            content = { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .testTag("libraryScreen")
                        .pullRefresh(pullRefreshState)
                        .fillMaxSize(),
                ) {
                    LazyColumn(
                        state = libraryListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                    ) {
                        item(key = "recent_books") {
                            val showRecent = showRecent()

                            when {
                                isPlaceholderRequired -> {
                                    RecentBooksPlaceholderComposable()
                                    Spacer(modifier = Modifier.height(20.dp))
                                }

                                showRecent -> {
                                    RecentBooksComposable(
                                        navigator = navigator,
                                        recentBooks = showingRecentBooks,
                                        libraryViewModel = libraryViewModel,
                                        settings = settings
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))
                                }
                            }
                        }

                        item(key = "library_title") {
                            if (!searchRequested && showRecent()) {
                                AnimatedContent(
                                    targetState = navBarTitle,
                                    transitionSpec = {
                                        fadeIn(
                                            animationSpec =
                                            tween(300),
                                        ) togetherWith fadeOut(
                                            animationSpec = tween(
                                                300,
                                            ),
                                        )
                                    },
                                    label = "library_header_fade",
                                ) {
                                    when {
                                        it == provideLibraryTitle() ->
                                            Spacer(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(titleHeightDp),
                                            )

                                        else -> Text(
                                            style = titleTextStyle,
                                            text = provideLibraryTitle(),
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                }
                            }
                        }

                        item(key = "library_spacer") { Spacer(modifier = Modifier.height(8.dp)) }

                        when {
                            isPlaceholderRequired -> item { LibraryPlaceholderComposable() }
                            library.itemCount == 0 -> {
                                item {
                                    LibraryFallbackComposable(
                                        searchRequested = searchRequested,
                                        networkQualityService = networkQualityService,
                                        libraryViewModel = libraryViewModel,
                                        settings = settings
                                    )
                                }
                            }

                            else -> items(count = library.itemCount, key = { "library_item_$it" }) {
                                val book = library[it] ?: return@items
                                val isVisible = remember(hiddenBooks, book.id) {
                                    derivedStateOf { libraryViewModel.isVisible(book.id) }
                                }

                                if (isVisible.value) {
                                    BookComposable(
                                        book = book,
                                        host = host,
                                        token = token,
                                        customHeaders = customHeaders,
                                        navigator = navigator,
                                        cachingViewModel = cachingViewModel,
                                        onRemoveBook = {
                                            // todo test and understand this
                                            if (!settings.isConnectedAndOnline()) {
                                                libraryViewModel.hideBook(book.id)

                                                val showingBooks = (0..<library.itemCount)
                                                    .mapNotNull { index -> library[index] }
                                                    .count { book -> libraryViewModel.isVisible(book.id) }

                                                if (showingBooks == 0) {
                                                    refreshContent(false)
                                                }
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }

                    if (!searchRequested) {
                        PullRefreshIndicator(
                            refreshing = pullRefreshing,
                            state = pullRefreshState,
                            contentColor = colorScheme.primary,
                            modifier = Modifier.align(Alignment.TopCenter),
                        )
                    }
                }
            },
        )
    }
}

private fun filterRecentBooks(
    books: List<RecentBook>,
    libraryViewModel: LibraryViewModel,
) = books.filter { libraryViewModel.isVisible(it.id) }
