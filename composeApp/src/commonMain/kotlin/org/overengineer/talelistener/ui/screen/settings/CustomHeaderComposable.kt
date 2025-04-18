
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.ui.screens.settings.advanced
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Migration to kotlin multiplatform
 */

package org.overengineer.talelistener.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.overengineer.talelistener.domain.connection.ServerRequestHeader
import talelistener.composeapp.generated.resources.Res
import talelistener.composeapp.generated.resources.custom_header_hint_name
import talelistener.composeapp.generated.resources.custom_header_hint_value


@Composable
fun CustomHeaderComposable(
    header: ServerRequestHeader,
    onChanged: (ServerRequestHeader) -> Unit,
    onDelete: (ServerRequestHeader) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorScheme.background),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                OutlinedTextField(
                    value = header.name,
                    onValueChange = { onChanged(header.copy(name = it, value = header.value)) },
                    label = { Text(stringResource(Res.string.custom_header_hint_name)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                )

                OutlinedTextField(
                    value = header.value,
                    onValueChange = { onChanged(header.copy(name = header.name, value = it)) },
                    label = { Text(stringResource(Res.string.custom_header_hint_value)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            IconButton(
                onClick = { onDelete(header) },
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete from cache",
                    tint = colorScheme.error,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
    }
}
