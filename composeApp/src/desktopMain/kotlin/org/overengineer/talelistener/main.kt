package org.overengineer.talelistener

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
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
        App()
    }
}

@Preview
@Composable
fun AppDesktopPreview() {
    App()
}
