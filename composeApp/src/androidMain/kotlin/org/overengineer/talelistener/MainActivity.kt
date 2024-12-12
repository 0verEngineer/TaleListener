package org.overengineer.talelistener

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.overengineer.talelistener.di.commonModule
import org.overengineer.talelistener.di.platformModule

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Napier.base(DebugAntilog())

        startKoin {
            androidContext(this@MainActivity)
            modules(commonModule(), platformModule())
        }

        setContent {
            App(
                darkTheme = isSystemInDarkTheme(),
                dynamicColor = true
            )
        }
    }
}
