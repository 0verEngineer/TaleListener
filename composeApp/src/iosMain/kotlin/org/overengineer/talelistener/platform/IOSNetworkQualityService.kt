package org.overengineer.talelistener.platform

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.overengineer.talelistener.content.TLMediaProvider
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences
import platform.Network.*
import platform.darwin.*

actual class NetworkQualityService(
    private val settings: TaleListenerSharedPreferences,
    private val mediaProvider: TLMediaProvider
) {
    private val monitor = nw_path_monitor_create()
    private val _networkStatus = MutableStateFlow(false)
    actual val networkStatus: StateFlow<Boolean> = _networkStatus.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        nw_path_monitor_set_update_handler(monitor) { path ->
            // path is of type nw_path_t
            val status = nw_path_get_status(path)
            val newNetworkStatus = (status == nw_path_status_satisfied)
            if (!_networkStatus.value && newNetworkStatus) {
                scope.launch { mediaProvider.checkTokenValidAndSetIsServerConnected() }
            }
            _networkStatus.value = newNetworkStatus
            settings.setIsOffline(!_networkStatus.value)
        }
        // Create a GCD queue and start monitoring
        val queue = dispatch_queue_create("NetworkMonitor", null)
        nw_path_monitor_set_queue(monitor, queue)
        nw_path_monitor_start(monitor)
    }

    actual fun isNetworkAvailable(): Boolean {
        return networkStatus.value
    }
}
