/*
 * Chronal: Metronome app for Android
 * Copyright (C) 2026  cognitivity
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.cognitivity.chronal.ui.metronome.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.metronome.MetronomeTrack
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.settings.types.json.MetronomeConfigTrack
import dev.cognitivity.chronal.settings.types.json.TrackColor
import kotlinx.coroutines.launch

data class TrackSettingsResult(
    val name: String,
    val enabled: Boolean,
    val vibrate: Boolean,
    val color: TrackColor,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TrackSettingsPage(
    track: MetronomeConfigTrack,
    onDismiss: () -> Unit,
    onSave: (TrackSettingsResult) -> Unit,
    canDelete: Boolean,
    onDelete: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var name by remember(track.name) { mutableStateOf(track.name) }
    var enabled by remember(track.enabled) { mutableStateOf(track.enabled) }
    var vibrate by remember(track.vibrate) { mutableStateOf(track.vibrate) }
    var color by remember(track.color) { mutableStateOf(track.color) }

    var showNameDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val previewColor = when(color) {
        TrackColor.Primary -> MaterialTheme.colorScheme.primary
        TrackColor.Secondary -> MaterialTheme.colorScheme.tertiary
        is TrackColor.Custom -> Color((color as TrackColor.Custom).value)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.track_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.generic_back),
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            onSave(
                                TrackSettingsResult(
                                    name = name.trim().ifBlank { track.name },
                                    enabled = enabled,
                                    vibrate = vibrate,
                                    color = color,
                                )
                            )
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(stringResource(R.string.generic_save))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 16.dp),
        ) {
            TrackSettingsToggle(track, enabled) { enabled = !enabled }

            val vibrationInteractionSource = remember { MutableInteractionSource() }
            TrackSettingsButton(
                text = stringResource(R.string.track_settings_vibrations),
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.outline_mobile_vibrate_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                },
                trailingContent = {
                    Switch(
                        checked = vibrate,
                        onCheckedChange = { vibrate = it },
                        interactionSource = vibrationInteractionSource,
                    )
                },
                topRounded = true,
                bottomRounded = false,
                onClick = {
                    vibrate = !vibrate
                }
            )

            TrackSettingsButton(
                text = stringResource(R.string.track_settings_rename),
                supportingText = name,
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                },
                topRounded = false,
                bottomRounded = false,
                onClick = {
                    showNameDialog = true
                }
            )

            val red = (previewColor.red * 255).toInt()
            val green = (previewColor.green * 255).toInt()
            val blue = (previewColor.blue * 255).toInt()
            TrackSettingsButton(
                text = stringResource(R.string.track_settings_recolor),
                supportingText = when (color) {
                    is TrackColor.Primary -> stringResource(R.string.track_settings_color_primary)
                    is TrackColor.Secondary -> stringResource(R.string.track_settings_color_secondary)
                    else -> stringResource(R.string.track_settings_color_rgb, red, green, blue)
                },
                leadingContent = {
                    Box(
                        modifier = Modifier.size(36.dp)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(previewColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_palette_24),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                topRounded = false,
                bottomRounded = true,
                onClick = {
                    showColorDialog = true
                }
            )
            if(canDelete) {
                Spacer(Modifier.height(16.dp))
                TrackSettingsButton(
                    text = stringResource(R.string.track_settings_delete),
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    topRounded = true,
                    bottomRounded = true,
                    onClick = {
                        showDeleteDialog = true
                    }
                )
            }
        }
    }

    if (showNameDialog) {
        var draftName by remember(name) { mutableStateOf(name) }
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text(stringResource(R.string.track_settings_name_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = draftName,
                    onValueChange = { draftName = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.track_settings_name)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        name = draftName.trim().ifBlank { name }
                        showNameDialog = false
                    }
                ) {
                    Text(stringResource(R.string.generic_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text(stringResource(R.string.generic_cancel))
                }
            }
        )
    }

    if (showColorDialog) {
        TrackSettingsColorDialog(
            color = color,
            onDismissRequest = { showColorDialog = false },
            onColorChange = {
                color = it
                showColorDialog = false
            }
        )
    }

    if(showDeleteDialog) {
        TrackSettingsDeleteDialog(MetronomeTrack.fromSetting(track), { showDeleteDialog = false }) {
            if(Settings.removeTrack(track)) {
                scope.launch {
                    Settings.METRONOME_CONFIG.save()
                }
                onDelete()
            } else {
                Toast.makeText(ChronalApp.getInstance(), R.string.track_settings_delete_failed, Toast.LENGTH_SHORT).show()
            }
            showDeleteDialog = false
        }
    }
}

