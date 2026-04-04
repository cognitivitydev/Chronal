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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.metronome.MetronomeTrack
import dev.cognitivity.chronal.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TrackList(tracks: List<MetronomeTrack>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(3.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.outline_library_music_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
            Text(context.getString(R.string.metronome_track_list, tracks.size),
                style = MaterialTheme.typography.titleMediumEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
                    .padding(horizontal = 8.dp)
            )
            FilledIconButton(
                onClick = {
                    // TODO
                },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.minimumInteractiveComponentSize()
                    .size(IconButtonDefaults.smallContainerSize(IconButtonDefaults.IconButtonWidthOption.Narrow)),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = context.getString(R.string.metronome_track_add)
                )
            }
        }
        tracks.forEachIndexed { index, track ->
            val topRounded = index == 0
            val bottomRounded = index == tracks.size - 1
            TrackItem(track, index, topRounded, bottomRounded)
        }
    }
}