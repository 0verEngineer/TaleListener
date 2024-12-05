package org.overengineer.talelistener.persistence.preferences

import com.russhwolf.settings.Settings
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.overengineer.talelistener.channel.common.ChannelCode
import org.overengineer.talelistener.channel.common.LibraryType
import org.overengineer.talelistener.common.AudioBookProgressBar
import org.overengineer.talelistener.common.ColorScheme
import org.overengineer.talelistener.domain.Library
import org.overengineer.talelistener.domain.connection.ServerRequestHeader
import org.overengineer.talelistener.platform.randomUUID

class TaleListenerSharedPreferences {

    private val settings: Settings = Settings()
    private val json = Json { ignoreUnknownKeys = true }

    fun hasCredentials(): Boolean {
        val host = getHost()
        val username = getUsername()
        val token = getToken()

        return try {
            host != null && username != null && token != null
        } catch (ex: Exception) {
            false
        }
    }

    fun clearPreferences() {
        removeMulti(listOf(
            KEY_HOST,
            KEY_USERNAME,
            KEY_TOKEN,
            KEY_SERVER_VERSION,
            CACHE_FORCE_ENABLED,
            KEY_PREFERRED_LIBRARY_ID,
            KEY_PREFERRED_LIBRARY_NAME,
            KEY_PREFERRED_PLAYBACK_SPEED
        ))
    }

    fun removeMulti(keys: List<String>) {
        for (key in keys) {
            settings.remove(key)
        }
    }

    fun saveHost(host: String) = settings.putString(KEY_HOST, host)

    fun getHost(): String? = settings.getStringOrNull(KEY_HOST)

    fun getDeviceId(): String {
        val existingDeviceId = settings.getStringOrNull(KEY_DEVICE_ID)

        if (existingDeviceId != null) {
            return existingDeviceId
        }

        val newId = randomUUID()
        settings.putString(KEY_DEVICE_ID, newId)
        return newId
    }

    fun getChannel() = ChannelCode.AUDIOBOOKSHELF

    fun getPreferredLibrary(): Library? {
        val id = getPreferredLibraryId() ?: return null
        val name = getPreferredLibraryName() ?: return null
        val type = getPreferredLibraryType() ?: LibraryType.LIBRARY

        return Library(
            id = id,
            title = name,
            type = type,
        )
    }

    fun savePreferredLibrary(library: Library) {
        saveActiveLibraryId(library.id)
        saveActiveLibraryName(library.title)
        saveActiveLibraryType(library.type)
    }

    fun saveColorScheme(colorScheme: ColorScheme) =
        settings.putString(KEY_PREFERRED_COLOR_SCHEME, colorScheme.name)

    fun getColorScheme(): ColorScheme =
        settings.getStringOrNull(KEY_PREFERRED_COLOR_SCHEME)
            ?.let { ColorScheme.valueOf(it) }
            ?: ColorScheme.FOLLOW_SYSTEM

    fun saveAudioBookProgressBar(progressBar: AudioBookProgressBar) =
        settings.putString(KEY_AUDIOBOOK_PROGRESS_BAR, progressBar.name)

    fun getAudioBookProgressBar(): AudioBookProgressBar =
        settings.getStringOrNull(KEY_AUDIOBOOK_PROGRESS_BAR)
            ?.let { AudioBookProgressBar.valueOf(it) }
            ?: AudioBookProgressBar.CHAPTER

    fun savePlaybackSpeed(factor: Float) =
        settings.putFloat(KEY_PREFERRED_PLAYBACK_SPEED, factor)

    fun getPlaybackSpeed(): Float =
        settings.getFloat(KEY_PREFERRED_PLAYBACK_SPEED, 1f)

    // todo needed? -> OnSharedPreferenceChangeListener is android only
    /*val colorSchemeFlow: Flow<ColorScheme> = callbackFlow {
        val listener = settings.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_PREFERRED_COLOR_SCHEME) {
                trySend(getColorScheme())
            }
        }
        settings.registerOnSharedPreferenceChangeListener(listener)
        trySend(getColorScheme())
        awaitClose { settings.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()*/

    private fun saveActiveLibraryId(host: String) =
        settings.putString(KEY_PREFERRED_LIBRARY_ID, host)

    private fun getPreferredLibraryId(): String? =
        settings.getStringOrNull(KEY_PREFERRED_LIBRARY_ID)

    private fun saveActiveLibraryName(host: String) =
        settings.putString(KEY_PREFERRED_LIBRARY_NAME, host)

    private fun getPreferredLibraryType(): LibraryType? =
        settings
            .getStringOrNull(KEY_PREFERRED_LIBRARY_TYPE)
            ?.let { LibraryType.valueOf(it) }

    private fun saveActiveLibraryType(type: LibraryType) =
        settings.putString(KEY_PREFERRED_LIBRARY_TYPE, type.name)

    private fun getPreferredLibraryName(): String? =
        settings.getStringOrNull(KEY_PREFERRED_LIBRARY_NAME)

    fun enableForceCache() =
        settings.putBoolean(CACHE_FORCE_ENABLED, true)

    fun disableForceCache() =
        settings.putBoolean(CACHE_FORCE_ENABLED, false)

    fun isForceCache(): Boolean {
        return settings.getBoolean(CACHE_FORCE_ENABLED, false)
    }

    fun saveUsername(username: String) =
        settings.putString(KEY_USERNAME, username)

    fun getUsername(): String? = settings.getStringOrNull(KEY_USERNAME)

    fun saveServerVersion(version: String) =
        settings.putString(KEY_SERVER_VERSION, version)

    fun getServerVersion(): String? = settings.getStringOrNull(KEY_SERVER_VERSION)

    fun saveToken(password: String) {
        settings.putString(KEY_TOKEN, password)
    }

    fun getToken(): String? {
        return settings.getStringOrNull(KEY_TOKEN)
    }

    fun saveCustomHeaders(headers: List<ServerRequestHeader>) {
        val serializedHeaders = json.encodeToString(ListSerializer(ServerRequestHeader.serializer()), headers)
        settings.putString(KEY_CUSTOM_HEADERS, serializedHeaders)
    }

    fun getCustomHeaders(): List<ServerRequestHeader> {
        val serializedHeaders = settings.getStringOrNull(KEY_CUSTOM_HEADERS) ?: return emptyList()
        return json.decodeFromString(ListSerializer(ServerRequestHeader.serializer()), serializedHeaders)
    }

    companion object {

        private const val KEY_HOST = "host"
        private const val KEY_USERNAME = "username"
        private const val KEY_TOKEN = "token"
        private const val CACHE_FORCE_ENABLED = "cache_force_enabled"

        private const val KEY_SERVER_VERSION = "server_version"

        private const val KEY_DEVICE_ID = "device_id"

        private const val KEY_PREFERRED_LIBRARY_ID = "preferred_library_id"
        private const val KEY_PREFERRED_LIBRARY_NAME = "preferred_library_name"
        private const val KEY_PREFERRED_LIBRARY_TYPE = "preferred_library_type"

        private const val KEY_PREFERRED_PLAYBACK_SPEED = "preferred_playback_speed"

        private const val KEY_PREFERRED_COLOR_SCHEME = "preferred_color_scheme"

        private const val KEY_AUDIOBOOK_PROGRESS_BAR = "audiobook_progress_bar"

        private const val KEY_CUSTOM_HEADERS = "custom_headers"
    }
}