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
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.activity.InstrumentActivity
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.ui.settings.items.SettingItem
import dev.cognitivity.chronal.ui.settings.items.SettingMeta
import dev.cognitivity.chronal.ui.settings.data.SettingsCategory
import dev.cognitivity.chronal.ui.settings.screens.tuner.A4FrequencyPage
import dev.cognitivity.chronal.ui.settings.screens.tuner.AccidentalsPage
import dev.cognitivity.chronal.ui.settings.screens.tuner.NoteLocalePage
import kotlin.math.roundToInt

object TunerCategory : SettingsCategory(
    id = "tuner",
    title = R.string.page_tuner,
    icon = R.drawable.baseline_graphic_eq_24,
    iconColor = { MaterialTheme.colorScheme.onPrimaryContainer },
    iconContainer = { MaterialTheme.colorScheme.primaryContainer },
    items = listOf(
        SettingItem.Switch(
            meta = SettingMeta(R.string.setting_name_transpose_notes, { Settings.PRIMARY_INSTRUMENT.get().name }),
            activity = InstrumentActivity::class.java,
            setting = Settings.TRANSPOSE_NOTES
        ),
        SettingItem.SubCategoryHeader(R.string.settings_header_note_display),
        SettingItem.Switch(
            meta = SettingMeta(R.string.setting_name_show_octave),
            setting = Settings.SHOW_OCTAVE
        ),
        SettingItem.PageLink(
            meta = SettingMeta(R.string.page_settings_note_locale,
                description = when(Settings.NOTE_NAMES.get()) {
                    0 -> R.string.setting_note_name_english
                    1 -> R.string.setting_note_name_solfege_english
                    2 -> R.string.setting_note_name_solfege_chromatic
                    3 -> R.string.setting_note_name_solfege_latin
                    4 -> R.string.setting_note_name_german
                    5 -> R.string.setting_note_name_nashville
                    else -> R.string.generic_unknown
                }
            ),
            pageId = NoteLocalePage.id
        ),
        SettingItem.PageLink(
            meta = SettingMeta(R.string.page_settings_accidentals,
                description = when(Settings.ACCIDENTALS.get()) {
                    0 -> R.string.setting_accidental_sharps
                    1 -> R.string.setting_accidental_flats
                    2 -> R.string.setting_accidentals_sharps_flats
                    else -> R.string.generic_unknown
                }
            ),
            pageId = AccidentalsPage.id
        ),
        SettingItem.Divider(),
        SettingItem.FloatSlider(
            meta = SettingMeta(R.string.setting_name_audio_threshold, R.string.setting_description_audio_threshold),
            range = 0.0f..1.0f,
            valueLabel = { "${(it * 100).roundToInt()}%" },
            setting = Settings.AUDIO_THRESHOLD
        ),
        SettingItem.PageLink(
            meta = SettingMeta(R.string.page_settings_a4_frequency, { context.getString(R.string.tuner_hz, Settings.TUNER_FREQUENCY.get()) }),
            pageId = A4FrequencyPage.id
        )
    )
)