package dev.cognitivity.chronal.ui.settings.screens.notifications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.notifications.PracticeReminderScheduler
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.ui.settings.data.SettingsPage
import dev.cognitivity.chronal.ui.settings.items.SettingItem
import dev.cognitivity.chronal.ui.settings.items.SettingMeta
import java.text.SimpleDateFormat
import java.util.TimeZone

private var time by mutableIntStateOf(Settings.PRACTICE_REMINDER_TIME.get())

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
                formatter.format(time * 60 * 1000L)
            }),
            onTimeSelected = {
                time = it
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