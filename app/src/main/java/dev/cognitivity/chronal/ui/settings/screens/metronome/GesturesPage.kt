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

package dev.cognitivity.chronal.ui.settings.screens.metronome

import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.ui.settings.data.SettingsPage
import dev.cognitivity.chronal.ui.settings.items.SettingItem
import dev.cognitivity.chronal.ui.settings.items.SettingMeta
import kotlin.math.roundToInt

object GesturesPage : SettingsPage(
    id = "metronome_gestures",
    title = R.string.page_settings_gestures,
    items = listOf(
        SettingItem.Switch(
            meta = SettingMeta(R.string.setting_name_metronome_gesture_tap, R.string.setting_description_metronome_gesture_tap),
            setting = Settings.GESTURE_TAP_ENABLED
        ),
        SettingItem.Divider(),
        SettingItem.Switch(
            meta = SettingMeta(R.string.setting_name_metronome_gesture_hold, R.string.setting_description_metronome_gesture_hold),
            setting = Settings.GESTURE_HOLD_ENABLED
        ),
        SettingItem.IntSlider(
            meta = SettingMeta(R.string.setting_name_metronome_gesture_hold_duration),
            range = 250..2500,
            steps = 17, // 125ms
            valueLabel = { "$it ms" },
            setting = Settings.GESTURE_HOLD_DURATION
        ),
        SettingItem.Divider(),
        SettingItem.Switch(
            meta = SettingMeta(R.string.setting_name_metronome_gesture_swipe, R.string.setting_description_metronome_gesture_swipe),
            setting = Settings.GESTURE_SWIPE_ENABLED
        ),
        SettingItem.FloatSlider(
            meta = SettingMeta(R.string.setting_name_metronome_gesture_swipe_sensitivity),
            range = 0.01f..0.99f,
            valueLabel = { "${(it * 100).roundToInt()}%" },
            setting = Settings.GESTURE_SWIPE_SENSITIVITY
        ),
        SettingItem.Switch(
            meta = SettingMeta(R.string.setting_name_metronome_gesture_swipe_invert),
            setting = Settings.GESTURE_SWIPE_INVERTED
        )
    )
)