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

package dev.cognitivity.chronal.ui.settings.screens.tuner

import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.ui.settings.items.SettingItem
import dev.cognitivity.chronal.ui.settings.data.SettingsPage

object NoteLocalePage : SettingsPage(
    id = "note_locale",
    title = R.string.page_settings_note_locale,
    items = listOf(
        SettingItem.RadioGroupItem(
            options = listOf(
                SettingItem.RadioOption(0, R.string.setting_note_name_english, R.string.setting_note_example_english),
                SettingItem.RadioOption(1, R.string.setting_note_name_solfege_english, R.string.setting_note_example_solfege_english),
                SettingItem.RadioOption(2, R.string.setting_note_name_solfege_chromatic, R.string.setting_note_example_solfege_chromatic),
                SettingItem.RadioOption(3, R.string.setting_note_name_solfege_latin, R.string.setting_note_example_solfege_latin),
                SettingItem.RadioOption(4, R.string.setting_note_name_german, R.string.setting_note_example_german),
                SettingItem.RadioOption(5, R.string.setting_note_name_nashville, R.string.setting_note_example_nashville)
            ),
            setting = Settings.NOTE_NAMES
        )
    )
)