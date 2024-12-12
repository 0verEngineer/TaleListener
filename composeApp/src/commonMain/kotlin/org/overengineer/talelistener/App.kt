package org.overengineer.talelistener

import androidx.compose.runtime.*
import cafe.adriel.voyager.navigator.Navigator
import io.github.aakira.napier.Napier
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.overengineer.talelistener.ui.screen.SplashScreen
import org.overengineer.talelistener.ui.theme.AppTheme


@Preview
@Composable
fun App(
    darkTheme: Boolean,
    dynamicColor: Boolean
) {
    Napier.d("darkTheme: $darkTheme")

    AppTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor
    ) {
        Navigator(screen = SplashScreen())
    }
}
