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

package dev.cognitivity.chronal.ui.settings.screens.notifications

import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.notifications.PracticeReminderScheduler
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.ui.settings.data.SettingsPage
import dev.cognitivity.chronal.ui.settings.items.SettingItem
import dev.cognitivity.chronal.ui.settings.items.SettingMeta
import java.text.SimpleDateFormat
import java.util.TimeZone

object PracticeReminderPage : SettingsPage(
    id = "practice_reminder",
    title = R.string.page_settings_practice_reminder,
    items = listOf(
        SettingItem.SwitchHeader(
            meta = SettingMeta(R.string.setting_name_practice_reminder_notification),
            setting = Settings.PRACTICE_REMINDER_NOTIFICATION
        ),
        SettingItem.LongDescription(R.string.setting_description_practice_reminder_notification),
        SettingItem.Divider(),
        SettingItem.TimeSelector(
            meta = SettingMeta(R.string.setting_name_practice_reminder_time, description = {
                val formatter = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)
                formatter.timeZone = TimeZone.getTimeZone("UTC")
                formatter.format(Settings.PRACTICE_REMINDER_TIME.get() * 60 * 1000L)
            }),
            onTimeSelected = {
                if(Settings.PRACTICE_REMINDER_NOTIFICATION.get()) {
                    PracticeReminderScheduler.scheduleDailyReminder(ChronalApp.context)
                }
            },
            setting = Settings.PRACTICE_REMINDER_TIME
        ),
        SettingItem.SubCategoryHeader(R.string.settings_header_practice_snooze),
        SettingItem.RadioGroupItem(
            setting = Settings.PRACTICE_REMINDER_SNOOZE,
            options = listOf(
                SettingItem.RadioOption(0, R.string.setting_name_practice_reminder_snooze_off),
                SettingItem.RadioOption(10, R.string.setting_name_practice_reminder_snooze_10),
                SettingItem.RadioOption(30, R.string.setting_name_practice_reminder_snooze_30),
                SettingItem.RadioOption(60, R.string.setting_name_practice_reminder_snooze_60),
                SettingItem.RadioOption(90, R.string.setting_name_practice_reminder_snooze_90),
            )
        )
    )
)