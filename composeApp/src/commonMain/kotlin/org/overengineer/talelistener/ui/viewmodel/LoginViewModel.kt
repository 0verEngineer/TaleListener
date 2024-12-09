
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.domain
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Changes for kotlin multiplatform
 * - Call onLoginFailure for every case in login function to always have correct _loginError
 */

package org.overengineer.talelistener.ui.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.overengineer.talelistener.channel.common.ApiError
import org.overengineer.talelistener.channel.common.LibraryType
import org.overengineer.talelistener.content.LissenMediaProvider
import org.overengineer.talelistener.domain.Library
import org.overengineer.talelistener.domain.UserAccount
import org.overengineer.talelistener.domain.error.LoginError
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences

class LoginViewModel(
    private val mediaChannel: LissenMediaProvider,
    private val preferences: TaleListenerSharedPreferences
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _loginError = MutableStateFlow<LoginError?>(null)
    val loginError: StateFlow<LoginError?> get() = _loginError.asStateFlow()

    private val _host = MutableStateFlow(preferences.getHost() ?: "")
    val host: StateFlow<String> get() = _host.asStateFlow()

    private val _username = MutableStateFlow(preferences.getUsername() ?: "")
    val username: StateFlow<String> get() = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> get() = _password.asStateFlow()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> get() = _loginState.asStateFlow()

    fun setHost(host: String) {
        _host.value = host
    }

    fun setUsername(username: String) {
        _username.value = username
    }

    fun setPassword(password: String) {
        _password.value = password
    }

    fun readyToLogin() {
        _loginState.value = LoginState.Idle
    }

    fun login() {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            val hostValue = host.value
            if (hostValue.isBlank()) {
                _loginState.value = onLoginFailure(ApiError.MissingCredentialsHost)
                return@launch
            }

            val usernameValue = username.value
            if (usernameValue.isBlank()) {
                _loginState.value = onLoginFailure(ApiError.MissingCredentialsUsername)
                return@launch
            }

            val passwordValue = password.value
            if (passwordValue.isBlank()) {
                _loginState.value = onLoginFailure(ApiError.MissingCredentialsPassword)
                return@launch
            }

            val response = mediaChannel.authorize(hostValue, usernameValue, passwordValue)

            response.foldAsync(
                onSuccess = { account ->
                    val successState = onLoginSuccessful(hostValue, usernameValue, account)
                    _loginState.value = successState
                },
                onFailure = { error ->
                    val errorState = onLoginFailure(error.code)
                    _loginState.value = errorState
                }
            )
        }
    }

    private suspend fun onLoginSuccessful(
        host: String,
        username: String,
        account: UserAccount
    ): LoginState.Success {
        persistCredentials(host, username, account.token)

        mediaChannel.fetchLibraries().fold(
            onSuccess = { libraries ->
                val preferredLibrary = libraries.find { it.id == account.preferredLibraryId } ?: libraries.firstOrNull()
                preferredLibrary?.let {
                    preferences.savePreferredLibrary(
                        Library(id = it.id, title = it.title, type = it.type)
                    )
                }
            },
            onFailure = {
                account.preferredLibraryId?.let { id ->
                    preferences.savePreferredLibrary(
                        Library(id = id, title = "Default Library", type = LibraryType.LIBRARY)
                    )
                }
            }
        )

        return LoginState.Success
    }

    private fun onLoginFailure(error: ApiError): LoginState.Error {
        _loginError.value = when (error) {
            ApiError.InternalError -> LoginError.InternalError
            ApiError.MissingCredentialsHost -> LoginError.MissingCredentialsHost
            ApiError.MissingCredentialsPassword -> LoginError.MissingCredentialsPassword
            ApiError.MissingCredentialsUsername -> LoginError.MissingCredentialsUsername
            ApiError.Unauthorized -> LoginError.Unauthorized
            ApiError.InvalidCredentialsHost -> LoginError.InvalidCredentialsHost
            ApiError.NetworkError -> LoginError.NetworkError
            ApiError.UnsupportedError -> LoginError.InternalError
        }

        return LoginState.Error(error)
    }

    private fun persistCredentials(host: String, username: String, token: String) {
        preferences.saveHost(host)
        preferences.saveUsername(username)
        preferences.saveToken(token)
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: ApiError) : LoginState()
    }
}
