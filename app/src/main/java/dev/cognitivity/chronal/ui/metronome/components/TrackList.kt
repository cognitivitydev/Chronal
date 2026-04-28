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

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.metronome.MetronomeTrack
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.activity.PresetActivity
import dev.cognitivity.chronal.activity.RhythmEditorActivity
import dev.cognitivity.chronal.activity.SimpleEditorActivity
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.settings.types.json.MetronomeConfigTrack
import dev.cognitivity.chronal.settings.types.json.SimpleRhythm
import dev.cognitivity.chronal.ui.metronome.MetronomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TrackList(viewModel: MetronomeViewModel, modifier: Modifier = Modifier) {
    val tracks by viewModel.tracks.collectAsState()
    var settingsDialogIndex by remember { mutableStateOf<Int?>(null) }

    fun openEditor(index: Int, track: MetronomeTrack) {
        if(track.simpleRhythm == SimpleRhythm.DISABLED) {
            ChronalApp.getInstance().startActivity(
                Intent(context, RhythmEditorActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra("trackIndex", index)
            )
        } else {
            ChronalApp.getInstance().startActivity(
                Intent(context, SimpleEditorActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra("trackIndex", index)
            )
        }
    }

    LazyColumn(
        modifier = modifier.padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
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
                FilledTonalButton(
                    onClick = {
                        ChronalApp.getInstance().startActivity(
                            Intent(context, PresetActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                ) {
                    Text(context.getString(R.string.presets_title))
                }
                Spacer(modifier = Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        val primaryTrack = tracks[0]
                        val primaryTimeSignature = primaryTrack.getRhythm().measures[0].timeSig

                        val simpleRhythm = SimpleRhythm(
                            timeSignature = primaryTimeSignature,
                            subdivision = primaryTimeSignature.second,
                            emphasis = 2
                        )

                        val newTrack = MetronomeTrack(
                            name = "New track",
                            rhythm = simpleRhythm.asRhythm(),
                            simpleRhythm = simpleRhythm,
                            beatValue = primaryTrack.beatValue
                        )
                        val trackIndex = Settings.addTrack(MetronomeConfigTrack.fromTrack(newTrack))
                        CoroutineScope(Dispatchers.Main).launch {
                            Settings.METRONOME_CONFIG.save()
                            ChronalApp.getInstance().startActivity(
                                Intent(context, SimpleEditorActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    .putExtra("trackIndex", trackIndex)
                            )
                        }

                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = context.getString(R.string.metronome_track_add)
                    )
                }
            }
        }
        itemsIndexed(tracks) { index, track ->
            val topRounded = index == 0
            val bottomRounded = index == tracks.size - 1
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TrackItem(track, topRounded, bottomRounded,
                    onCheckedChanged = { enabled ->
                        viewModel.setTrackEnabled(index, enabled)
                    },
                    onClick = {
                        openEditor(index, track)
                    },
                    onLongClick = {
                        settingsDialogIndex = index
                    }
                )

                TrackSettingsDropdown(
                    track = track,
                    expanded = settingsDialogIndex == index,
                    canDelete = tracks.count { it != track && it.enabled } != 0,
                    isOutsideEditor = true,
                    onDismissRequest = {
                        if(settingsDialogIndex == index) settingsDialogIndex = null
                    },
                    onEdit = {
                        openEditor(index, track)
                    },
                    onDeleteFinish = {
                        settingsDialogIndex = null
                        viewModel.reloadMetronomeState()
                    },
                    onSwitchEditor = {}
                )
            }
        }
    }
}