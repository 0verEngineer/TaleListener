package org.overengineer.talelistener.platform

import kotlinx.coroutines.flow.StateFlow

// todo: if they also return false (android and ios) if the wifi we are in has no internet
//  connection we will need to refactor some things -> same on desktop
expect class NetworkQualityService {
    val networkStatus: StateFlow<Boolean>
    fun isNetworkAvailable(): Boolean
}
