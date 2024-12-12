package org.overengineer.talelistener

import androidx.compose.ui.window.ComposeUIViewController
import org.koin.core.context.startKoin
import org.overengineer.talelistener.di.commonModule
import org.overengineer.talelistener.di.platformModule
import platform.UIKit.UIScreen
import platform.UIKit.UIUserInterfaceStyle

fun MainViewController() = ComposeUIViewController {

    val isDarkTheme =
        UIScreen.mainScreen.traitCollection.userInterfaceStyle ==
                UIUserInterfaceStyle.UIUserInterfaceStyleDark
    App(
        darkTheme = isDarkTheme,
        dynamicColor = false,
    )
}