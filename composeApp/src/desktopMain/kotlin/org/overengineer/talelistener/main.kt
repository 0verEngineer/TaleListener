package org.overengineer.talelistener

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

fun main() = application {
    Napier.base(DebugAntilog())

    Window(
        onCloseRequest = ::exitApplication,
        title = "TaleListener",
    ) {
        App(
            darkTheme = isSystemInDarkTheme(), // Todo: test on MacOS (with auto switching) and Windows
            dynamicColor = false
        )
    }
}

