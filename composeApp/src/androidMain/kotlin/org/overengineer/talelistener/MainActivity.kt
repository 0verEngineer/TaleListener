package org.overengineer.talelistener

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Napier.base(DebugAntilog())

        setContent {
            App(
                darkTheme = isSystemInDarkTheme(),
                dynamicColor = true
            )
        }
    }
}
