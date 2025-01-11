
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.ui.viewmodel
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Migration to kotlin multiplatform
 */

package org.overengineer.talelistener.ui.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.overengineer.talelistener.channel.common.ApiResult
import org.overengineer.talelistener.common.AudioBookProgressBar
import org.overengineer.talelistener.common.ColorScheme
import org.overengineer.talelistener.content.TLMediaProvider
import org.overengineer.talelistener.domain.Library
import org.overengineer.talelistener.domain.connection.ServerRequestHeader
import org.overengineer.talelistener.domain.connection.ServerRequestHeader.Companion.clean
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences

class SettingsViewModel(
    private val mediaChannel: TLMediaProvider,
    private val preferences: TaleListenerSharedPreferences
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _host = MutableStateFlow(preferences.getHost())
    val host = _host

    private val _serverVersion = MutableStateFlow(preferences.getServerVersion())
    val serverVersion = _serverVersion

    private val _username = MutableStateFlow(preferences.getUsername())
    val username = _username

    private val _libraries = MutableStateFlow<List<Library>>(emptyList())
    val libraries = _libraries

    private val _preferredLibrary = MutableStateFlow(preferences.getPreferredLibrary())
    val preferredLibrary = _preferredLibrary

    private val _preferredColorScheme = MutableStateFlow(preferences.getColorScheme())
    val preferredColorScheme = _preferredColorScheme

    private val _audioBookProgressBar = MutableStateFlow(preferences.getAudioBookProgressBar())
    val audioBookProgressBar = _audioBookProgressBar

    private val _customHeaders = MutableStateFlow(preferences.getCustomHeaders())
    val customHeaders = _customHeaders

    fun logout() {
        preferences.clearPreferences()
    }

    fun refreshConnectionInfo() {
        viewModelScope.launch {
            when (val response = mediaChannel.fetchConnectionInfo()) {
                is ApiResult.Error -> Unit
                is ApiResult.Success -> {
                    _username.value = response.data.username
                    _serverVersion.value = response.data.serverVersion

                    updateServerInfo()
                }
            }
        }
    }

    fun fetchLibraries() {
        viewModelScope.launch {
            when (val response = mediaChannel.fetchLibraries()) {
                is ApiResult.Success -> {
                    val libraries = response.data
                    _libraries.value = libraries

                    val preferredLibrary = preferences.getPreferredLibrary()
                    _preferredLibrary.value = when (preferredLibrary) {
                        null -> libraries.firstOrNull()
                        else -> libraries.find { it.id == preferredLibrary.id }
                    }
                }

                is ApiResult.Error -> {
                    _libraries.value = preferences.getPreferredLibrary()?.let { listOf(it) }!!
                }
            }
        }
    }

    fun preferLibrary(library: Library) {
        _preferredLibrary.value = library
        preferences.savePreferredLibrary(library)
    }

    fun preferColorScheme(colorScheme: ColorScheme) {
        _preferredColorScheme.value = colorScheme
        preferences.saveColorScheme(colorScheme)
    }

    fun updateAudioBookProgressBar(progressBar: AudioBookProgressBar) {
        _audioBookProgressBar.value = progressBar
        preferences.saveAudioBookProgressBar(progressBar)
    }

    fun updateCustomHeaders(headers: List<ServerRequestHeader>) {
        _customHeaders.value = headers

        val meaningfulHeaders = headers
            .map { it.clean() }
            .distinctBy { it.name }
            .filterNot { it.name.isEmpty() }
            .filterNot { it.value.isEmpty() }

        preferences.saveCustomHeaders(meaningfulHeaders)
    }

    private fun updateServerInfo() {
        serverVersion.value?.let { preferences.saveServerVersion(it) }
        username.value?.let { preferences.saveUsername(it) }
    }}