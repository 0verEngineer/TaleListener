package org.overengineer.talelistener.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

// todo:
//  - macOS theme switching not working
@Composable
actual fun AppTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if(darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}