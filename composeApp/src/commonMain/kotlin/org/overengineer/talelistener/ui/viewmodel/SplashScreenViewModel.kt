package org.overengineer.talelistener.ui.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.overengineer.talelistener.content.TLMediaProvider
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences

class SplashScreenViewModel(
    private val settings: TaleListenerSharedPreferences,
    private val mediaChannel: TLMediaProvider
) {

    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _tokenValid: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    val tokenValid: StateFlow<Boolean?> get() = _tokenValid.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> get() = _isConnected.asStateFlow()

    fun hasCredentials(): Boolean {
        return settings.hasCredentials()
    }

    fun isTokenValid() {
        viewModelScope.launch {
            _tokenValid.value = mediaChannel.checkTokenValidAndSetIsServerConnected()
            _isConnected.value = settings.isConnectedAndOnline()
        }
    }
}