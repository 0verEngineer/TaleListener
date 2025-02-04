
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.ui.screens.settings
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Migration to kotlin multiplatform
 */

package org.overengineer.talelistener.ui.screen.settings.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import org.jetbrains.compose.resources.stringResource
import org.overengineer.talelistener.ui.screen.LoginScreen
import org.overengineer.talelistener.ui.viewmodel.SettingsViewModel
import talelistener.composeapp.generated.resources.Res
import talelistener.composeapp.generated.resources.settings_screen_connected_as_title
import talelistener.composeapp.generated.resources.settings_screen_server_connection
import talelistener.composeapp.generated.resources.settings_screen_server_version

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerSettingsComposable(
    viewModel: SettingsViewModel,
    navigator: Navigator
) {
    var connectionInfoExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshConnectionInfo()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { connectionInfoExpanded = true }
            .padding(start = 24.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.settings_screen_server_connection),
                style = typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(bottom = 4.dp),
            )

            Text(
                text = "${viewModel.host.value}",
                style = typography.bodyMedium,
                maxLines = 1,
                modifier = Modifier.padding(bottom = 4.dp),
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(
            onClick = {
                viewModel.logout()
                navigator.replaceAll(LoginScreen())
            },
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "Logout",
            )
        }
    }

    if (connectionInfoExpanded) {
        ModalBottomSheet(
            containerColor = MaterialTheme.colorScheme.background,
            onDismissRequest = { connectionInfoExpanded = false },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .padding(horizontal = 16.dp),
                ) {
                    viewModel.username.value?.let {
                        InfoRow(
                            label = stringResource(Res.string.settings_screen_connected_as_title),
                            value = it,
                        )
                        HorizontalDivider()
                    }
                    viewModel.serverVersion.value?.let {
                        InfoRow(
                            label = stringResource(Res.string.settings_screen_server_version),
                            value = it,
                        )
                    }
                }
            },
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    ListItem(
        headlineContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = label)
                Text(text = value)
            }
        },
    )
}
