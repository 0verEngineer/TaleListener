
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.ui.screens.settings
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Migration to kotlin multiplatform
 * - Removed the toItem methods and created the items inline
 */

package org.overengineer.talelistener.ui.screen.settings.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.NotInterested
import androidx.compose.material.icons.outlined.Podcasts
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.overengineer.talelistener.channel.common.LibraryType
import org.overengineer.talelistener.common.AudioBookProgressBar
import org.overengineer.talelistener.common.ColorScheme
import org.overengineer.talelistener.ui.viewmodel.SettingsViewModel
import talelistener.composeapp.generated.resources.Res
import talelistener.composeapp.generated.resources.audiobook_progress_bar_book
import talelistener.composeapp.generated.resources.audiobook_progress_bar_chapter
import talelistener.composeapp.generated.resources.audiobook_progress_bar_title
import talelistener.composeapp.generated.resources.library_is_not_available
import talelistener.composeapp.generated.resources.preferred_library
import talelistener.composeapp.generated.resources.theme
import talelistener.composeapp.generated.resources.theme_dark
import talelistener.composeapp.generated.resources.theme_follow_system
import talelistener.composeapp.generated.resources.theme_light

@Composable
fun CommonSettingsComposable(viewModel: SettingsViewModel) {
    val libraries by viewModel.libraries.collectAsState(emptyList())
    val preferredLibrary by viewModel.preferredLibrary.collectAsState()
    val preferredColorScheme by viewModel.preferredColorScheme.collectAsState()
    val audioBookProgressBar by viewModel.audioBookProgressBar.collectAsState()

    val host by viewModel.host.collectAsState("")
    var preferredLibraryExpanded by remember { mutableStateOf(false) }
    var colorSchemeExpanded by remember { mutableStateOf(false) }
    var audioBookProgressBarExpanded by remember { mutableStateOf(false) }

    val string_theme_follow_system = stringResource(Res.string.theme_follow_system)
    val string_theme_light = stringResource(Res.string.theme_light)
    val string_theme_dark = stringResource(Res.string.theme_dark)
    val string_audiobook_progress_bar_chapter = stringResource(Res.string.audiobook_progress_bar_chapter)
    val string_audiobook_progress_bar_book = stringResource(Res.string.audiobook_progress_bar_book)

    SideEffect {
        viewModel.fetchLibraries()
    }

    if (host?.isNotEmpty() == true) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { preferredLibraryExpanded = true }
                .padding(horizontal = 24.dp, vertical = 12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.preferred_library),
                    style = typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                Text(
                    text = preferredLibrary?.title
                        ?: stringResource(Res.string.library_is_not_available),
                    style = typography.bodyMedium,
                    color = when (preferredLibrary?.title) {
                        null -> colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        else -> colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { colorSchemeExpanded = true }
            .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = stringResource(Res.string.theme),
                style = typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Text(
                text = when (preferredColorScheme) {
                    ColorScheme.FOLLOW_SYSTEM -> string_theme_follow_system
                    ColorScheme.LIGHT -> string_theme_light
                    ColorScheme.DARK -> string_theme_dark
                },
                style = typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
            )
        }
    }

    Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .clickable { audioBookProgressBarExpanded = true }
                    .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        Column(
                modifier = Modifier.weight(1f),
        ) {
            Text(
                    text = stringResource(Res.string.audiobook_progress_bar_title),
                    style = typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(bottom = 4.dp),
            )
            Text(
                    text = when (audioBookProgressBar) {
                        AudioBookProgressBar.CHAPTER -> string_audiobook_progress_bar_chapter
                        AudioBookProgressBar.BOOK -> string_audiobook_progress_bar_book
                    },
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
            )
        }
    }

    if (preferredLibraryExpanded && libraries.isNotEmpty()) {
        CommonSettingsItemComposable(
            items = libraries.map { CommonSettingsItem(it.id, it.title, it.type.provideIcon()) },
            selectedItem = preferredLibrary?.let { CommonSettingsItem(it.id, it.title, it.type.provideIcon()) },
            onDismissRequest = { preferredLibraryExpanded = false },
            onItemSelected = { item ->
                libraries
                    .find { it.id == item.id }
                    ?.let { viewModel.preferLibrary(it) }
            },
        )
    }

    if (colorSchemeExpanded) {
        CommonSettingsItemComposable(
            items = listOf(
                CommonSettingsItem(ColorScheme.FOLLOW_SYSTEM.name, string_theme_follow_system, null),
                CommonSettingsItem(ColorScheme.DARK.name, string_theme_dark, null),
                CommonSettingsItem(ColorScheme.LIGHT.name, string_theme_light, null),
            ),
            selectedItem = CommonSettingsItem(
                preferredColorScheme.name,
                when (preferredColorScheme) {
                    ColorScheme.FOLLOW_SYSTEM -> string_theme_follow_system
                    ColorScheme.LIGHT -> string_theme_light
                    ColorScheme.DARK -> string_theme_dark
                },
                null
            ),
            onDismissRequest = { colorSchemeExpanded = false },
            onItemSelected = { item ->
                ColorScheme
                    .entries
                    .find { it.name == item.id }
                    ?.let { viewModel.preferColorScheme(it) }
            },
        )
    }

    if (audioBookProgressBarExpanded) {
        CommonSettingsItemComposable(
                items = listOf(
                    CommonSettingsItem(AudioBookProgressBar.CHAPTER.name, string_audiobook_progress_bar_chapter, null),
                    CommonSettingsItem(AudioBookProgressBar.BOOK.name, string_audiobook_progress_bar_book, null)
                ),
                selectedItem = CommonSettingsItem(
                    audioBookProgressBar.name,
                    when (audioBookProgressBar) {
                        AudioBookProgressBar.BOOK -> string_audiobook_progress_bar_book
                        AudioBookProgressBar.CHAPTER -> string_audiobook_progress_bar_chapter
                    },
                    null
                ),
                onDismissRequest = { audioBookProgressBarExpanded = false },
                onItemSelected = { item ->
                    AudioBookProgressBar
                            .entries
                            .find { it.name == item.id }
                            ?.let { viewModel.updateAudioBookProgressBar(it) }
                },
        )
    }
}

private fun LibraryType.provideIcon() = when (this) {
    LibraryType.LIBRARY -> Icons.Outlined.Book
    LibraryType.PODCAST -> Icons.Outlined.Podcasts
    LibraryType.UNKNOWN -> Icons.Outlined.NotInterested
}
