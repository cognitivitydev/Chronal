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

package dev.cognitivity.chronal.ui.settings.screens.appinfo

import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.settings.types.consts.StartScreen
import dev.cognitivity.chronal.ui.settings.data.SettingsPage
import dev.cognitivity.chronal.ui.settings.items.SettingItem

object StartScreenPage : SettingsPage(
    id = "start_screen",
    title = R.string.page_settings_start_screen,
    items = listOf(
        SettingItem.RadioGroupItem(
            setting = Settings.START_SCREEN,
            options = listOf(
                SettingItem.RadioOption(StartScreen.METRONOME, R.string.page_metronome),
                SettingItem.RadioOption(StartScreen.TUNER, R.string.page_tuner)
            )
        )
    )
)
