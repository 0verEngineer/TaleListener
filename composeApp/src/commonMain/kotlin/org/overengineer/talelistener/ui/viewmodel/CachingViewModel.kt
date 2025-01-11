
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.viewmodel
 * Original file name: CachingModelView
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Migration to kotlin multiplatform
 * - Removed not needed methods
 */

package org.overengineer.talelistener.ui.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.overengineer.talelistener.common.BookCacheAction
import org.overengineer.talelistener.content.TLMediaProvider
import org.overengineer.talelistener.content.cache.CacheProgress
import org.overengineer.talelistener.domain.Book
import org.overengineer.talelistener.domain.BookCachedState
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences

class CachingViewModel(
    private val settings: TaleListenerSharedPreferences,
    private val mediaProvider: TLMediaProvider
) {
    private val _bookCachingProgress = mutableMapOf<String, MutableStateFlow<CacheProgress>>()

    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun provideCacheAction(book: Book): BookCacheAction? {
        return when (getCacheProgress(book.id).value) {
            CacheProgress.Caching -> null
            CacheProgress.Completed -> BookCacheAction.DROP
            CacheProgress.Error -> BookCacheAction.CACHE
            CacheProgress.Idle -> when (book.cachedState) {
                BookCachedState.ABLE_TO_CACHE -> BookCacheAction.CACHE
                BookCachedState.CACHED -> BookCacheAction.DROP
                BookCachedState.UNABLE_TO_CACHE -> null
            }

            CacheProgress.Removed -> BookCacheAction.CACHE
        }
    }

    fun dropCache(book: Book) {
        viewModelScope.launch {
            // todo caching
            /*cachingService
                .removeBook(book)
                .collect {
                    _bookCachingProgress[book.id]?.value = it
                }*/
        }
    }

    fun cacheBook(book: Book) {
        viewModelScope.launch {
            // todo caching
            /*cachingService
                .cacheBook(book, mediaProvider.providePreferredChannel())
                .collect { _bookCachingProgress[book.id]?.value = it }*/
        }
    }

    fun getCacheProgress(bookId: String) = _bookCachingProgress
        .getOrPut(bookId) {
            MutableStateFlow(CacheProgress.Idle)
        }
}