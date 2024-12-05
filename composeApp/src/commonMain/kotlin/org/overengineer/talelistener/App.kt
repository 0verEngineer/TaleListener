package org.overengineer.talelistener

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import org.koin.compose.koinInject
import org.overengineer.talelistener.ui.screen.LoginScreen
import org.overengineer.talelistener.ui.viewmodel.LoginViewModel


@Composable
fun App() {
    MaterialTheme {
        // Inject LoginViewModel using Koin
        val loginViewModel: LoginViewModel = koinInject()
        // Display the LoginScreen
        LoginScreen(loginViewModel)
    }
}
