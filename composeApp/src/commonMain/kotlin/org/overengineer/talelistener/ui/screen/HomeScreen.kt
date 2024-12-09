package org.overengineer.talelistener.ui.screen

import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen

class HomeScreen: Screen {
    @Composable
    override fun Content() {

        Scaffold {
            Text("HomeScreen")
        }
    }
}