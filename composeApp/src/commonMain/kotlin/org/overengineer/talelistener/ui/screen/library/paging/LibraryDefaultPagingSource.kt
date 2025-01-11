package org.overengineer.talelistener.ui.screen.library.paging

/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.ui.screens.library.paging
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Use app.cash.paging instead of androidx.paging
 */

import app.cash.paging.PagingSource
import app.cash.paging.PagingState
import org.overengineer.talelistener.content.TLMediaProvider
import org.overengineer.talelistener.domain.Book
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences

class LibraryDefaultPagingSource(
    private val preferences: TaleListenerSharedPreferences,
    private val mediaChannel: TLMediaProvider
) : PagingSource<Int, Book>() {

    override fun getRefreshKey(state: PagingState<Int, Book>) = state
        .anchorPosition
        ?.let { anchorPosition ->
            state
                .closestPageToPosition(anchorPosition)
                ?.prevKey
                ?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Book> {
        val libraryId = preferences
            .getPreferredLibrary()
            ?.id
            ?: return LoadResult.Page(emptyList(), null, null)

        return mediaChannel
            .fetchBooks(
                libraryId = libraryId,
                pageSize = params.loadSize,
                pageNumber = params.key ?: 0,
            )
            .fold(
                onSuccess = { result ->
                    val nextPage = if (result.items.isEmpty()) null else result.currentPage + 1
                    val prevKey = if (result.currentPage == 0) null else result.currentPage - 1

                    LoadResult.Page(
                        data = result.items,
                        prevKey = prevKey,
                        nextKey = nextPage,
                    )
                },
                onFailure = {
                    LoadResult.Page(emptyList(), null, null)
                },
            )
    }
}
