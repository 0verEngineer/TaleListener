
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.viewmodel
 * Original file name: LibraryViewModel
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Use app.cash.paging instead of androidx.paging
 */

package org.overengineer.talelistener.ui.viewmodel

import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.overengineer.talelistener.channel.common.LibraryType
import org.overengineer.talelistener.content.TLMediaProvider
import org.overengineer.talelistener.domain.Book
import org.overengineer.talelistener.domain.RecentBook
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences
import org.overengineer.talelistener.ui.screen.library.paging.LibraryDefaultPagingSource
import org.overengineer.talelistener.ui.screen.library.paging.LibrarySearchPagingSource

class LibraryViewModel(
    private val settings: TaleListenerSharedPreferences,
    private val mediaChannel: TLMediaProvider
) {

    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _recentBooks = MutableStateFlow<List<RecentBook>>(emptyList())
    val recentBooks: StateFlow<List<RecentBook>> = _recentBooks.asStateFlow()

    private val _recentBookUpdating = MutableStateFlow(false)
    val recentBookUpdating: StateFlow<Boolean> = _recentBookUpdating.asStateFlow()

    private val _searchRequested = MutableStateFlow(false)
    val searchRequested: StateFlow<Boolean> = _searchRequested.asStateFlow()

    private val _searchToken = MutableStateFlow(EMPTY_SEARCH)

    private val _hiddenBooks = MutableStateFlow<List<String>>(emptyList())
    val hiddenBooks: StateFlow<List<String>> = _hiddenBooks

    private val pageConfig = PagingConfig(
        pageSize = PAGE_SIZE,
        initialLoadSize = PAGE_SIZE,
        prefetchDistance = PAGE_SIZE,
    )

    // todo: Is it possible without flatMapLatest / the OptIn
    // Search Pager (Dynamically Updates Based on Search Token)
    @OptIn(ExperimentalCoroutinesApi::class)
    val searchPager: Flow<PagingData<Book>> = combine(
        _searchToken,
        _searchRequested
    ) { token, _ ->
        token
    }.flatMapLatest { token ->
        Pager(
            config = pageConfig,
            pagingSourceFactory = {
                LibrarySearchPagingSource(
                    preferences = settings,
                    mediaChannel = mediaChannel,
                    searchToken = token,
                    limit = PAGE_SIZE
                )
            }
        ).flow.cachedIn(viewModelScope)
    }

    // Library Pager (Default Library Paging)
    val libraryPager: Flow<PagingData<Book>> by lazy {
        Pager(
            config = pageConfig,
            pagingSourceFactory = {
                LibraryDefaultPagingSource(settings, mediaChannel)
            }
        ).flow.cachedIn(viewModelScope)
    }

    // Search Visibility
    fun isVisible(bookId: String): Boolean {
        return if (!settings.isConnectedAndOnline()) {
            !hiddenBooks.value.contains(bookId)
        } else {
            true
        }
    }

    // Search Functions
    fun requestSearch() {
        _searchRequested.value = true
    }

    fun dismissSearch() {
        _searchRequested.value = false
        _searchToken.value = EMPTY_SEARCH
    }

    fun updateSearch(token: String) {
        viewModelScope.launch {
            _searchToken.emit(token)
        }
    }

    // Library Management
    fun fetchPreferredLibraryType(): LibraryType {
        return settings.getPreferredLibrary()?.type ?: LibraryType.UNKNOWN
    }

    fun refreshRecentListening() {
        viewModelScope.launch(Dispatchers.IO) {
            fetchRecentListening()
        }
    }

    fun refreshLibrary() {
        viewModelScope.launch(Dispatchers.IO) {
            libraryPager.collect {}  // Re-trigger flow to reload the library
        }
    }

    suspend fun fetchRecentListening() {
        _recentBookUpdating.value = true
        val preferredLibrary = settings.getPreferredLibrary()?.id ?: run {
            _recentBookUpdating.value = false
            return
        }

        mediaChannel.fetchRecentListenedBooks(preferredLibrary)
            .fold(
                onSuccess = {
                    _recentBooks.value = it
                    _recentBookUpdating.value = false
                },
                onFailure = {
                    _recentBookUpdating.value = false
                }
            )
    }

    // Hidden Books Management
    fun hideBook(bookId: String) {
        _hiddenBooks.value += bookId
    }

    fun dropHiddenBooks() {
        _hiddenBooks.value = emptyList()
    }

    companion object {
        private const val EMPTY_SEARCH = ""
        private const val PAGE_SIZE = 20
    }
}