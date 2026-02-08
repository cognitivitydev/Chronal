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

import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.ui.settings.items.SettingItem
import dev.cognitivity.chronal.ui.settings.items.SettingMeta
import dev.cognitivity.chronal.ui.settings.data.SettingsPage

object A4FrequencyPage : SettingsPage(
    id = "a4_frequency",
    title = R.string.page_settings_a4_frequency,
    items = listOf(
        SettingItem.IntSlider(
            meta = SettingMeta(R.string.page_settings_a4_frequency),
            range = 415..466,
            valueLabel = { context.getString(R.string.tuner_hz, it) },
            setting = Settings.TUNER_FREQUENCY
        ),
        SettingItem.LongDescription(R.string.setting_description_tuner_frequency)
    )
)