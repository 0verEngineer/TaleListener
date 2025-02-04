
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import talelistener.composeapp.generated.resources.Res
import talelistener.composeapp.generated.resources.audiobook_progress_bar_description

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonSettingsItemComposable(
    items: List<CommonSettingsItem>,
    selectedItem: CommonSettingsItem?,
    onDismissRequest: () -> Unit,
    onItemSelected: (CommonSettingsItem) -> Unit,
) {
    var activeItem by remember { mutableStateOf(selectedItem) }

    ModalBottomSheet(
        containerColor = MaterialTheme.colorScheme.background,
        onDismissRequest = onDismissRequest,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .padding(horizontal = 16.dp),
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                // todo implement the progress bar stuff
                // todo text style?
                // todo test text overflow
                // todo variable
                Text(
                        text = stringResource(Res.string.audiobook_progress_bar_description),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(items) { item ->
                        ListItem(
                            leadingContent = {
                                item.icon?.let {
                                    Icon(
                                        imageVector = it,
                                        contentDescription = "Settings Item Icon",
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            },
                            headlineContent = {
                                Row { Text(item.name) }
                            },
                            trailingContent = {
                                if (item.id == activeItem?.id) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    activeItem = item
                                    onItemSelected(item)
                                    onDismissRequest()
                                },
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
    )
}
