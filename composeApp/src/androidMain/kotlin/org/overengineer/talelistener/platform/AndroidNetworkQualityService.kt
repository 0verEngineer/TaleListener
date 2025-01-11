
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.common
 * Original file name: NetworkQualityService
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Implement TaleListeners NetworkQualityService interface
 * - Added settings to set the isOffline setting accordingly
 * - Refactor the isNetworkAvailable to also set isOffline and check the token
 */

package org.overengineer.talelistener.platform

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.overengineer.talelistener.content.TLMediaProvider
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences

actual class NetworkQualityService (
    private val context: Context,
    private val settings: TaleListenerSharedPreferences,
    private val mediaProvider: TLMediaProvider
) {

    private val connectivityManager =
        context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _networkStatus = MutableStateFlow(false)
    actual val networkStatus: StateFlow<Boolean> = _networkStatus.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        registerNetworkCallback()
    }

    actual fun isNetworkAvailable(): Boolean {
        val isNetworkAvailable = pIsNetworkAvailable()
        settings.setIsOffline(!isNetworkAvailable)
        if (isNetworkAvailable) {
            scope.launch { mediaProvider.checkTokenValidAndSetIsServerConnected() }
        }
        return isNetworkAvailable
    }

    private fun pIsNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun registerNetworkCallback() {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (!_networkStatus.value) {
                    scope.launch { mediaProvider.checkTokenValidAndSetIsServerConnected() }
                }
                _networkStatus.value = true
                settings.setIsOffline(false)
            }

            override fun onLost(network: Network) {
                _networkStatus.value = false
                settings.setIsOffline(true)
            }
        }

        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }
}
