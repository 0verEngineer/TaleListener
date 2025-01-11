package org.overengineer.talelistener

import androidx.compose.runtime.*
import cafe.adriel.voyager.navigator.Navigator
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.request.CachePolicy
import coil3.request.crossfade
import coil3.util.DebugLogger
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

    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .logger(DebugLogger())
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    AppTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor
    ) {
        Navigator(screen = SplashScreen())
    }
}
