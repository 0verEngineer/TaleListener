package org.overengineer.talelistener.ui.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.overengineer.talelistener.content.TLMediaProvider
import org.overengineer.talelistener.domain.DetailedItem
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences

// todo: this is only a workaround for now to fetch one item for testing
class PlayerViewModel(
    private val mediaChannel: TLMediaProvider,
    private val settings: TaleListenerSharedPreferences
) {

    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _book = MutableStateFlow<DetailedItem?>(null)
    val book: StateFlow<DetailedItem?> get() = _book.asStateFlow()

    fun loadBook(bookId: String) {
        viewModelScope.launch {
            mediaChannel.fetchBook(bookId).fold(
                onSuccess = {
                    _book.value = it
                },
                onFailure = {

                }
            )
        }
    }
}