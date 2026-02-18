/*
 * Chronal: Metronome app for Android
 * Copyright (C) 2025-2026  cognitivity
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

package dev.cognitivity.chronal.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.cognitivity.chronal.settings.Settings

class PracticeReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            ACTION_SNOOZE -> {
                val snoozeMinutes = intent.getIntExtra(SNOOZE_MINUTES, 0)
                if (snoozeMinutes > 0) {
                    PracticeReminderScheduler.scheduleSnooze(context, snoozeMinutes)
                }
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                if (Settings.PRACTICE_REMINDER_NOTIFICATION.get()) {
                    PracticeReminderScheduler.scheduleDailyReminder(context)
                }
            }
        }
    }

    companion object {
        const val ACTION_SNOOZE = "dev.cognitivity.chronal.PRACTICE_REMINDER_SNOOZE"
        const val SNOOZE_MINUTES = "snooze_minutes"
    }
}

