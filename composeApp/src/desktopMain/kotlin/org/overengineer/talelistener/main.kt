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

    Napier.d("Starting Koin")
    val koinApp = startKoin {
        modules(commonModule(), platformModule())
    }.koin
    Napier.d("Koin started")

    val networkService = koinApp.get<NetworkQualityService>()
    val audioPlayer = koinApp.get<AudioPlayerDesktop>()

    Napier.d("Creating app/window now")

    Window(
        onCloseRequest = {
            Napier.d("Window close request")
            networkService.close()
            audioPlayer.release()
            Napier.d("Cleanup complete, exiting now")
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

