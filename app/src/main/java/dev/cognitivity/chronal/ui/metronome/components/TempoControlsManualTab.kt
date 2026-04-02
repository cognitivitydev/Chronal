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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.metronome.Metronome
import dev.cognitivity.chronal.metronome.MetronomeTrack
import dev.cognitivity.chronal.ui.metronome.MetronomeViewModel

@Composable
fun ManualTab(metronome: Metronome, viewModel: MetronomeViewModel) {
    var text by remember { mutableStateOf(metronome.bpm.toString().replace(Regex("\\.0$"), "")) }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            val bpm = text.toFloatOrNull() ?: return@OutlinedTextField
            if(bpm in MetronomeTrack.MIN_BPM..MetronomeTrack.MAX_BPM) {
                viewModel.setBpm(bpm, vibrate = false)
            }
        },
        textStyle = MaterialTheme.typography.bodyLarge,
        isError = text.toFloatOrNull() == null ||
                text.toFloat() < MetronomeTrack.MIN_BPM || text.toFloat() > MetronomeTrack.MAX_BPM,
        label = { Text(context.getString(R.string.metronome_input_manual)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        suffix = { Text(context.getString(R.string.metronome_bpm)) },
        modifier = Modifier
            .widthIn(80.dp, 160.dp)
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    )
}
