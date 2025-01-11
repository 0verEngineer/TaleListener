/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.ui.screens.settings.advanced
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Migration to kotlin multiplatform
 * - Changed it to a Screen
 */

package org.overengineer.talelistener.ui.screen.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.overengineer.talelistener.domain.connection.ServerRequestHeader
import org.overengineer.talelistener.ui.viewmodel.SettingsViewModel
import talelistener.composeapp.generated.resources.Res
import talelistener.composeapp.generated.resources.custom_headers_title
import kotlin.math.max

class CustomHeadersSettingsScreen: Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val settingsViewModel = koinInject<SettingsViewModel>()
        val navigator = LocalNavigator.currentOrThrow
        val headers = settingsViewModel.customHeaders.collectAsState(emptyList())

        val fabHeight = 56.dp
        val additionalPadding = 16.dp

        val state = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(Res.string.custom_headers_title),
                            style = typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = colorScheme.onSurface,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            navigator.pop()
                        }) {
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
                LazyColumn(
                    state = state,
                    contentPadding = PaddingValues(
                        top = innerPadding.calculateTopPadding(),
                        bottom = innerPadding.calculateBottomPadding() + fabHeight + additionalPadding,
                    ),
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val customHeaders = when (headers.value.isEmpty()) {
                        true -> listOf(ServerRequestHeader.empty())
                        false -> headers.value
                    }

                    itemsIndexed(customHeaders) { index, header ->
                        CustomHeaderComposable(
                            header = header,
                            onChanged = { newPair ->
                                val updatedList = customHeaders.toMutableList()
                                updatedList[index] = newPair

                                settingsViewModel.updateCustomHeaders(updatedList)
                            },
                            onDelete = { pair ->
                                val updatedList = customHeaders.toMutableList()
                                updatedList.remove(pair)

                                if (updatedList.isEmpty()) {
                                    updatedList.add(ServerRequestHeader.empty())
                                }

                                settingsViewModel.updateCustomHeaders(updatedList)
                            },
                        )

                        if (index < customHeaders.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier
                                    .height(1.dp)
                                    .padding(horizontal = 24.dp),
                            )
                        }
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
            floatingActionButton = {
                FloatingActionButton(
                    containerColor = colorScheme.primary,
                    shape = CircleShape,
                    onClick = {
                        val updatedList = headers.value.toMutableList()
                        updatedList.add(ServerRequestHeader.empty())
                        settingsViewModel.updateCustomHeaders(updatedList)

                        coroutineScope.launch {
                            state.scrollToItem(max(0, updatedList.size - 1))
                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add",
                    )
                }
            },
        )
    }
}
