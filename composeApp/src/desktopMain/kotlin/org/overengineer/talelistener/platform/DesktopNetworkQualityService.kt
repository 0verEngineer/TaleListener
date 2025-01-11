package org.overengineer.talelistener.platform

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import org.overengineer.talelistener.content.TLMediaProvider
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences
import java.net.HttpURLConnection
import java.net.URI


actual class NetworkQualityService(
    private val settings: TaleListenerSharedPreferences,
    private val mediaProvider: TLMediaProvider
) {

    private val _networkStatus = MutableStateFlow(false)
    actual val networkStatus: StateFlow<Boolean> = _networkStatus.asStateFlow()

    private val checkJob = Job()
    private val checkScope = CoroutineScope(Dispatchers.IO + checkJob)
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        checkScope.launch {
            while (isActive) {
                val newStatus = isNetworkAvailable()
                if (_networkStatus.value != newStatus && newStatus) {
                    checkToken()
                }
                _networkStatus.value = newStatus
                delay(5000)
            }
        }
    }

    actual fun isNetworkAvailable(): Boolean {
        val isNetworkAvailable = pIsNetworkAvailable()
        settings.setIsOffline(!isNetworkAvailable)
        return isNetworkAvailable
    }

    fun close() {
        checkJob.cancel()
    }

    private fun checkToken() {
        scope.launch { mediaProvider.checkTokenValidAndSetIsServerConnected() }
    }

    private fun pIsNetworkAvailable(): Boolean {
        return try {
            var host = settings.getHost()
            if (host == null) {
                host = "https://google.com"
            }

            val url = URI(host).toURL()
            (url.openConnection() as? HttpURLConnection)?.run {
                connectTimeout = 3000
                connect()
                // A 2xx response suggests we’re online
                responseCode in 200..299
            } ?: false
        } catch (e: IOException) {
            false
        }
    }
}