package org.overengineer.talelistener

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.overengineer.talelistener.di.initKoin

fun main() = application {
    Napier.base(DebugAntilog())

    initKoin()

    Window(
        onCloseRequest = ::exitApplication,
        title = "TaleListener",
    ) {
        App()
    }
}