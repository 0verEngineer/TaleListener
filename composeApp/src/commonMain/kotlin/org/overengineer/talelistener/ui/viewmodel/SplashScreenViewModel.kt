package org.overengineer.talelistener.ui.viewmodel

import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences

class SplashScreenViewModel(
    private val settings: TaleListenerSharedPreferences
) {
    fun hasCredentials(): Boolean {
        return settings.hasCredentials()
    }
}