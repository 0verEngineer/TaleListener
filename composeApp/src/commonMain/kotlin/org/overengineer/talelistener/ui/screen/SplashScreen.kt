package org.overengineer.talelistener.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.koinInject
import org.overengineer.talelistener.ui.viewmodel.SplashScreenViewModel

class SplashScreen: Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinInject<SplashScreenViewModel>()

        LaunchedEffect(Unit) {
            if (viewModel.hasCredentials()) {
                navigator.replaceAll(HomeScreen())
            } else {
                navigator.replaceAll(LoginScreen())
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(
                color = Color.Blue, // todo theming
                strokeWidth = 4.dp,
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }
    }
}