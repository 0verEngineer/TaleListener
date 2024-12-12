package org.overengineer.talelistener

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.core.context.startKoin
import org.overengineer.talelistener.di.commonModule
import org.overengineer.talelistener.di.platformModule

fun main() = application {
    Napier.base(DebugAntilog())

    startKoin {
        modules(commonModule(), platformModule())
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "TaleListener",
    ) {
        App(
            darkTheme = isSystemInDarkTheme(), // Todo: test on Windows
            dynamicColor = false
        )
    }
}

