package org.overengineer.talelistener.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

        Scaffold (
            modifier = Modifier
                .systemBarsPadding()
                .fillMaxSize(),

            content = { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp,
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                }
            }
        )
    }
}