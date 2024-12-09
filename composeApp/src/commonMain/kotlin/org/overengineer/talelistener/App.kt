package org.overengineer.talelistener

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import cafe.adriel.voyager.navigator.Navigator
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.overengineer.talelistener.di.commonModule
import org.overengineer.talelistener.ui.screen.SplashScreen


@Preview
@Composable
fun App() {
    KoinApplication(application = {
        modules(commonModule())
    }) {
        MaterialTheme {
            Navigator(screen = SplashScreen())
        }
    }
}