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

package dev.cognitivity.chronal.metronome.tracks

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import dev.cognitivity.chronal.settings.types.json.metronome.MetronomeConfigAudioTrack
import dev.cognitivity.chronal.settings.types.json.metronome.TrackColor

class AudioTrack(
    override var name: String = uri.lastPathSegment ?: "New audio track",
    override var color: TrackColor = TrackColor.Secondary,
    enabled: Boolean = true,
    var uri: Uri,
    var fileName: String,
    var startTrim: Float = 0f,
    var endTrim: Float = 0f,
    var volume: Float = 1f,
    var loop: Boolean = false,
    var bpm: Float?
): MetronomeTrack(name, color, enabled) {
    companion object {
        fun fromSetting(setting: MetronomeConfigAudioTrack): AudioTrack {
            return AudioTrack(
                name = setting.name,
                color = setting.color,
                enabled = setting.enabled,
                uri = setting.uri.toUri(),
                fileName = setting.fileName,
                startTrim = setting.startTrim,
                endTrim = setting.endTrim,
                volume = setting.volume,
                loop = setting.loop,
                bpm = setting.bpm
            )
        }
    }

    override var enabled by mutableStateOf(enabled)

    var error by mutableStateOf(false)
    fun exists(context: Context): Boolean = try {
        context.contentResolver.openAssetFileDescriptor(uri, "r")?.use {
            true
        } ?: false
    } catch(_: Exception) {
        false
    }
}