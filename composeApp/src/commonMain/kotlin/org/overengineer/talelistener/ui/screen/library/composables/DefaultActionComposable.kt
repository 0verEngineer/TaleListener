
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.screens.library.composables
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Migration to kotlin multiplatform
 */

package org.overengineer.talelistener.ui.screen.library.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import org.jetbrains.compose.resources.stringResource
import org.overengineer.talelistener.ui.screen.settings.SettingsScreen
import org.overengineer.talelistener.ui.viewmodel.CachingViewModel
import org.overengineer.talelistener.ui.viewmodel.LibraryViewModel
import talelistener.composeapp.generated.resources.Res
import talelistener.composeapp.generated.resources.settings

// todo cleanup
@Composable
fun DefaultActionComposable(
    navigator: Navigator,
    cachingViewModel: CachingViewModel,
    libraryViewModel: LibraryViewModel,
    onContentRefreshing: (Boolean) -> Unit,
    onSearchRequested: () -> Unit,
) {
    var navigationItemSelected by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Row {
        IconButton(
            onClick = { onSearchRequested() },
            modifier = Modifier.offset(x = 4.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
            )
        }
        IconButton(onClick = {
            navigationItemSelected = true
        }) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = "Menu",
            )
        }
    }

    DropdownMenu(
        expanded = navigationItemSelected,
        onDismissRequest = { navigationItemSelected = false },
        modifier = Modifier
            .background(colorScheme.background)
            .padding(4.dp),
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null,
                )
            },
            text = {
                Text(
                    stringResource(Res.string.settings),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp),
                )
            },
            onClick = {
                navigationItemSelected = false
                navigator.push(SettingsScreen())
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        )
    }
}
