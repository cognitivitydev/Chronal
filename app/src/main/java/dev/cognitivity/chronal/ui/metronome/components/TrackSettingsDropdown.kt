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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.metronome.MetronomeTrack
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.settings.types.json.MetronomeConfigTrack
import dev.cognitivity.chronal.settings.types.json.SimpleRhythm
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TrackSettingsDropdown(track: MetronomeTrack, expanded: Boolean, canDelete: Boolean,
                          onDismissRequest: () -> Unit,
                          onEdit: () -> Unit,
                          onDeleteFinish: () -> Unit,
                          onSwitchEditor: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }

    DropdownMenuPopup(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
    ) {
        DropdownMenuGroup(
            shapes = MenuDefaults.groupShape(0, 2),
        ) {
            MenuDefaults.Label {
                Text(track.name)
            }
            HorizontalDivider(
                modifier = Modifier.padding(MenuDefaults.HorizontalDividerPadding)
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.track_settings_edit)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                        contentDescription = null
                    )
                },
                shape = MenuDefaults.itemShape(1, 3).shape,
                onClick = {
                    onDismissRequest()
                    onEdit()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.track_settings_delete)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                        contentDescription = null
                    )
                },
                shape = MenuDefaults.itemShape(2, 3).shape,
                onClick = {
                    onDismissRequest()
                    showDeleteDialog = true
                },
                enabled = canDelete
            )
        }
        Spacer(modifier = Modifier.height(MenuDefaults.GroupSpacing))
        DropdownMenuGroup(
            shapes = MenuDefaults.groupShape(1, 2),
            containerColor = MenuDefaults.groupVibrantContainerColor
        ) {
            val isAdvanced = track.simpleRhythm == SimpleRhythm.DISABLED
            DropdownMenuItem(
                text = { Text(stringResource(if(isAdvanced) R.string.simple_editor_switch_simple else R.string.simple_editor_switch_advanced)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Build,
                        modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                        contentDescription = null
                    )
                },
                shape = MenuDefaults.itemShape(0, 1).shape,
                colors = MenuDefaults.selectableItemVibrantColors(),
                onClick = {
                    onSwitchEditor()
                }
            )
        }
    }

    if(showDeleteDialog) {
        TrackSettingsDeleteDialog(track, { showDeleteDialog = false }) {
            if(Settings.removeTrack(MetronomeConfigTrack.fromTrack(track))) {
                scope.launch {
                    Settings.METRONOME_CONFIG.save()
                }
                onDeleteFinish()
            } else {
                Toast.makeText(ChronalApp.getInstance(), R.string.track_settings_delete_failed, Toast.LENGTH_SHORT).show()
            }
            showDeleteDialog = false
        }
    }
}