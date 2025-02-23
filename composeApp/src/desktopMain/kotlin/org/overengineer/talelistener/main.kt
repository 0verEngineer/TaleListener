package org.overengineer.talelistener

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.core.context.startKoin
import org.overengineer.talelistener.di.commonModule
import org.overengineer.talelistener.di.platformModule
import org.overengineer.talelistener.platform.AudioPlayerDesktop
import org.overengineer.talelistener.platform.NetworkQualityService

fun main() = application {
    Napier.base(DebugAntilog())

    val koinApp = startKoin {
        modules(commonModule(), platformModule())
    }.koin

    val networkService = koinApp.get<NetworkQualityService>()
    val audioPlayer = koinApp.get<AudioPlayerDesktop>()

    Window(
        onCloseRequest = {
            networkService.close()
            audioPlayer.release()
            exitApplication()
        },
        title = "TaleListener",
    ) {
        App(
            darkTheme = isSystemInDarkTheme(), // Todo: test on Windows
            dynamicColor = false
        )
    }
}

