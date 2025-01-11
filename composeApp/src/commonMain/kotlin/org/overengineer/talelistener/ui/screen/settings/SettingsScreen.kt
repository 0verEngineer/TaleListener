
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.ui.screens.settings
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Use koin for di
 */

package org.overengineer.talelistener.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.overengineer.talelistener.ui.screen.settings.composeable.AdditionalComposable
import org.overengineer.talelistener.ui.screen.settings.composeable.AdvancedSettingsItemComposable
import org.overengineer.talelistener.ui.screen.settings.composeable.CommonSettingsComposable
import org.overengineer.talelistener.ui.screen.settings.composeable.ServerSettingsComposable
import org.overengineer.talelistener.ui.viewmodel.SettingsViewModel
import talelistener.composeapp.generated.resources.Res
import talelistener.composeapp.generated.resources.custom_headers_hint
import talelistener.composeapp.generated.resources.custom_headers_title
import talelistener.composeapp.generated.resources.settings

class SettingsScreen: Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = koinInject<SettingsViewModel>()
        val host by viewModel.host.collectAsState("")
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(Unit) {
            viewModel.fetchLibraries()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(Res.string.settings),
                            style = typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = colorScheme.onSurface,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = colorScheme.onSurface,
                            )
                        }
                    },
                )
            },
            modifier = Modifier
                .systemBarsPadding()
                .fillMaxHeight(),
            content = { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (host?.isNotEmpty() == true) {
                            ServerSettingsComposable(viewModel, navigator)
                        }
                        CommonSettingsComposable(viewModel)
                        AdvancedSettingsItemComposable(
                            title = stringResource(Res.string.custom_headers_title),
                            description = stringResource(Res.string.custom_headers_hint),
                            onclick = { navigator.push(CustomHeadersSettingsScreen() ) },
                        )
                    }
                    AdditionalComposable()
                }
            },
        )
    }
}