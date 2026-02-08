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

package dev.cognitivity.chronal.ui.settings.categories

import androidx.compose.material3.MaterialTheme
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.activity.LatencyActivity
import dev.cognitivity.chronal.activity.TempoMarkingsActivity
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.ui.settings.items.SettingItem
import dev.cognitivity.chronal.ui.settings.items.SettingMeta
import dev.cognitivity.chronal.ui.settings.data.SettingsCategory

object MetronomeCategory : SettingsCategory(
    id = "metronome",
    title = R.string.page_metronome,
    icon = R.drawable.baseline_music_note_24,
    iconColor = { MaterialTheme.colorScheme.onPrimaryContainer },
    iconContainer = { MaterialTheme.colorScheme.primaryContainer },
    items = listOf(
        SettingItem.ActivityLink(
            meta = SettingMeta(R.string.setting_name_visual_latency, description = { "${Settings.VISUAL_LATENCY.get()} ms" }),
            activity = LatencyActivity::class.java
        ),
        SettingItem.ActivityLink(
            meta = SettingMeta(R.string.setting_name_tempo_markings),
            activity = TempoMarkingsActivity::class.java
        ),
        SettingItem.SubCategoryHeader(R.string.settings_header_beat_display),
        SettingItem.Switch(
            meta = SettingMeta(R.string.setting_name_show_beats),
            setting = Settings.SHOW_BEATS
        ),
        SettingItem.Switch(
            meta = SettingMeta(R.string.setting_name_show_subdivisions),
            setting = Settings.SHOW_SUBDIVISIONS
        ),
        SettingItem.SubCategoryHeader(R.string.metronome_option_fullscreen_mode),
        SettingItem.Switch(
            meta = SettingMeta(R.string.setting_name_high_contrast),
            setting = Settings.HIGH_CONTRAST
        ),
        SettingItem.Switch(
            meta = SettingMeta(R.string.setting_name_no_animation),
            setting = Settings.NO_ANIMATION
        ),
    )
)