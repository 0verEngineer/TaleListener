
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.ui.screens.library.paging
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Use app.cash.paging instead of androidx.paging
 */

package org.overengineer.talelistener.ui.screen.library.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import org.overengineer.talelistener.content.TLMediaProvider
import org.overengineer.talelistener.domain.Book
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences

class LibrarySearchPagingSource(
    private val preferences: TaleListenerSharedPreferences,
    private val mediaChannel: TLMediaProvider,
    private val searchToken: String,
    private val limit: Int,
) : PagingSource<Int, Book>() {

    override fun getRefreshKey(state: PagingState<Int, Book>) = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Book> {
        val libraryId = preferences
            .getPreferredLibrary()
            ?.id
            ?: return LoadResult.Page(emptyList(), null, null)

        if (searchToken.isBlank()) {
            return LoadResult.Page(emptyList(), null, null)
        }

        return mediaChannel
            .searchBooks(libraryId, searchToken, limit)
            .fold(
                onSuccess = { LoadResult.Page(it, null, null) },
                onFailure = { LoadResult.Page(emptyList(), null, null) },
            )
    }
}
